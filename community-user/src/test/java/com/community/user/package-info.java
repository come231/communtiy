/**
 * <h2>用户服务（community-user）测试包说明</h2>
 *
 * <p><b>是否应拆成「数据访问 / 业务 / 控制」三层测试？</b></p>
 * <ul>
 *   <li><b>数据访问层（Mapper / Repository）</b>：验证 SQL 映射、MyBatis-Plus 行为（分页、逻辑删除、字段填充等）。
 *       常用 {@code @MybatisPlusTest} + 嵌入式 H2，或与生产一致的集成环境（Testcontainers + MySQL）。</li>
 *   <li><b>业务层（Service）</b>：验证业务规则、分支与异常；依赖全部用 Mockito 模拟，<b>不连真实库</b>，速度快、定位准。</li>
 *   <li><b>控制层（Controller）</b>：验证 HTTP 契约（路径、方法、参数绑定、校验、响应 JSON 结构）；使用 {@code @WebMvcTest}
 *       + MockMvc，Service 用 {@code @MockBean}，<b>不测业务细节</b>（业务在 Service 测）。</li>
 * </ul>
 *
 * <p><b>标准 Maven 目录结构（与 main 包名镜像）</b></p>
 * <pre>
 * community-user/
 * └── src/test/
 *     ├── java/com/community/user/
 *     │   ├── controller/     ← Web 层切片测试（MockMvc）
 *     │   ├── service/        ← 业务层纯单元测试（Mockito）
 *     │   └── mapper/         ← 数据访问层切片测试（@MybatisPlusTest + H2）
 *     └── resources/
 *         ├── application-test.yml   ← 测试专用配置（数据源、MyBatis 等）
 *         └── schema-h2.sql          ← 测试库表结构（可选）
 * </pre>
 *
 * <p>说明：端到端（全链路 + 真实中间件）通常单独放在 {@code src/test/java/.../integration/} 或独立模块，
 * 不在此包强制要求，以免与「单元/切片」混淆。</p>
 */
package com.community.user;
