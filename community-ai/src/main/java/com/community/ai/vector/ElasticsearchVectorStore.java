package com.community.ai.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "content.audit.vector",
        name = "store-type",
        havingValue = "elasticsearch"
)
public class ElasticsearchVectorStore implements VectorStore {

    private final RestHighLevelClient restHighLevelClient;
    private final EmbeddingClient embeddingClient;

    @Value("${content.audit.vector.index-name:ai_risk_vector_index}")
    private String indexName;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            ensureIndex();
            log.info("ElasticsearchVectorStore 初始化完成, index={}, size={}", indexName, size());
        } catch (Exception e) {
            log.error("ElasticsearchVectorStore 初始化失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void upsert(String id, String content, double[] vector, String label) {
        Map<String, Object> doc = buildDocument(content, vector, label);
        IndexRequest request = new IndexRequest(indexName).id(id).source(doc);
        try {
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("向量写入ES失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void upsertBatch(List<VectorDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        BulkRequest bulkRequest = new BulkRequest();
        for (VectorDocument doc : documents) {
            Map<String, Object> source = buildDocument(doc.content(), doc.vector(), doc.label());
            bulkRequest.add(new IndexRequest(indexName).id(doc.id()).source(source));
        }
        try {
            BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (response.hasFailures()) {
                log.warn("向量批量写入ES存在失败项: {}", response.buildFailureMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("向量批量写入ES失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ScoredVector> search(double[] queryVector, int topK, double minScore) {
        if (queryVector == null || queryVector.length == 0 || topK <= 0) {
            return Collections.emptyList();
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(topK);
        sourceBuilder.fetchSource(new String[]{"content", "label"}, new String[]{});

        Map<String, Object> params = new HashMap<>();
        params.put("queryVector", queryVector);
        Script script = new Script(
                ScriptType.INLINE,
                "painless",
                "(cosineSimilarity(params.queryVector, 'vector') + 1.0) / 2.0",
                params
        );
        sourceBuilder.query(QueryBuilders.scriptScoreQuery(QueryBuilders.matchAllQuery(), script));
        SearchRequest request = new SearchRequest(indexName).source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            List<ScoredVector> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                double score = hit.getScore();
                if (score < minScore) {
                    continue;
                }
                Map<String, Object> source = hit.getSourceAsMap();
                String content = String.valueOf(source.getOrDefault("content", ""));
                String label = String.valueOf(source.getOrDefault("label", ""));
                results.add(new ScoredVector(hit.getId(), content, label, score));
            }
            return results;
        } catch (IOException e) {
            log.error("向量检索失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteByLabel(String label) {
        if (label == null || label.isBlank()) {
            return;
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.termQuery("label.keyword", label))
                .size(5000)
                .fetchSource(false);
        SearchRequest searchRequest = new SearchRequest(indexName).source(sourceBuilder);
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            List<String> ids = Arrays.stream(response.getHits().getHits()).map(SearchHit::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                return;
            }
            BulkRequest bulkRequest = new BulkRequest();
            for (String id : ids) {
                bulkRequest.add(new org.elasticsearch.action.delete.DeleteRequest(indexName, id));
            }
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("按标签删除向量: label={}, 删除{}条", label, ids.size());
        } catch (IOException e) {
            throw new RuntimeException("按标签删除向量失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int size() {
        try {
            CountResponse response = restHighLevelClient.count(new CountRequest(indexName), RequestOptions.DEFAULT);
            return (int) response.getCount();
        } catch (Exception e) {
            log.warn("获取向量库大小失败: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public void clear() {
        try {
            restHighLevelClient.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
        } catch (Exception ignored) {
        }
        try {
            ensureIndex();
            log.info("向量库已清空并重建 index={}", indexName);
        } catch (Exception e) {
            throw new RuntimeException("清空向量库失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<VectorDocument> list(int limit) {
        int n = limit > 0 ? limit : 20;
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .size(n)
                .sort("createTime", SortOrder.DESC)
                .fetchSource(new String[]{"content", "label", "vector"}, new String[]{});
        SearchRequest request = new SearchRequest(indexName).source(sourceBuilder);
        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            List<VectorDocument> docs = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                String content = String.valueOf(source.getOrDefault("content", ""));
                String label = String.valueOf(source.getOrDefault("label", ""));
                double[] vector = toDoubleArray(source.get("vector"));
                docs.add(new VectorDocument(hit.getId(), content, vector, label));
            }
            return docs;
        } catch (Exception e) {
            log.warn("读取向量样本列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, Object> buildDocument(String content, double[] vector, String label) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("content", content != null ? content : "");
        doc.put("label", label != null ? label : "");
        doc.put("vector", vector);
        doc.put("createTime", System.currentTimeMillis());
        return doc;
    }

    private void ensureIndex() throws IOException {
        GetIndexRequest getRequest = new GetIndexRequest(indexName);
        boolean exists = restHighLevelClient.indices().exists(getRequest, RequestOptions.DEFAULT);
        if (exists) {
            return;
        }
        int dims = embeddingClient.getDimensions();
        String mappingJson = """
                {
                  "properties": {
                    "content": { "type": "text" },
                    "label": { "type": "keyword" },
                    "createTime": { "type": "date", "format": "epoch_millis" },
                    "vector": { "type": "dense_vector", "dims": %d }
                  }
                }
                """.formatted(dims);
        CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
        createRequest.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0));
        createRequest.mapping(mappingJson, XContentType.JSON);
        restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
    }

    private double[] toDoubleArray(Object source) {
        if (!(source instanceof List<?> list) || list.isEmpty()) {
            return new double[0];
        }
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i);
            if (v instanceof Number num) {
                arr[i] = num.doubleValue();
            } else {
                try {
                    arr[i] = Double.parseDouble(String.valueOf(v));
                } catch (Exception ignored) {
                    arr[i] = 0D;
                }
            }
        }
        return arr;
    }
}
