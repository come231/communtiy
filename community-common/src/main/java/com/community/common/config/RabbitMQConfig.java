package com.community.common.config;

import com.community.common.constant.MQConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange communityExchange() {
        return new TopicExchange(MQConstant.COMMUNITY_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange aiExchange() {
        return new TopicExchange(MQConstant.AI_EXCHANGE, true, false);
    }

    // 互动队列
    @Bean
    public Queue likeQueue() {
        return QueueBuilder.durable(MQConstant.LIKE_QUEUE).build();
    }

    @Bean
    public Queue collectQueue() {
        return QueueBuilder.durable(MQConstant.COLLECT_QUEUE).build();
    }

    @Bean
    public Queue followQueue() {
        return QueueBuilder.durable(MQConstant.FOLLOW_QUEUE).build();
    }

    @Bean
    public Queue noticeQueue() {
        return QueueBuilder.durable(MQConstant.NOTICE_QUEUE).build();
    }

    // 搜索同步队列
    @Bean
    public Queue searchSyncQueue() {
        return QueueBuilder.durable(MQConstant.SEARCH_SYNC_QUEUE).build();
    }

    // AI任务队列
    @Bean
    public Queue aiTaskQueue() {
        return QueueBuilder.durable(MQConstant.AI_TASK_QUEUE).build();
    }

    @Bean
    public Queue aiSummaryQueue() {
        return QueueBuilder.durable(MQConstant.AI_SUMMARY_QUEUE).build();
    }

    @Bean
    public Queue aiReviewQueue() {
        return QueueBuilder.durable(MQConstant.AI_REVIEW_QUEUE).build();
    }

    // 绑定
    @Bean
    public Binding likeBinding() {
        return BindingBuilder.bind(likeQueue()).to(communityExchange()).with(MQConstant.LIKE_ROUTING_KEY);
    }

    @Bean
    public Binding collectBinding() {
        return BindingBuilder.bind(collectQueue()).to(communityExchange()).with(MQConstant.COLLECT_ROUTING_KEY);
    }

    @Bean
    public Binding followBinding() {
        return BindingBuilder.bind(followQueue()).to(communityExchange()).with(MQConstant.FOLLOW_ROUTING_KEY);
    }

    @Bean
    public Binding noticeBinding() {
        return BindingBuilder.bind(noticeQueue()).to(communityExchange()).with(MQConstant.NOTICE_ROUTING_KEY);
    }

    @Bean
    public Binding searchSyncBinding() {
        return BindingBuilder.bind(searchSyncQueue()).to(communityExchange()).with(MQConstant.SEARCH_SYNC_ROUTING_KEY);
    }

    @Bean
    public Binding aiTaskBinding() {
        return BindingBuilder.bind(aiTaskQueue()).to(aiExchange()).with(MQConstant.AI_TASK_ROUTING_KEY);
    }

    @Bean
    public Binding aiSummaryBinding() {
        return BindingBuilder.bind(aiSummaryQueue()).to(aiExchange()).with(MQConstant.AI_SUMMARY_ROUTING_KEY);
    }

    @Bean
    public Binding aiReviewBinding() {
        return BindingBuilder.bind(aiReviewQueue()).to(aiExchange()).with(MQConstant.AI_REVIEW_ROUTING_KEY);
    }
}
