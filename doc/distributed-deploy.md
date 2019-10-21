# 分布式部署

Duo-GraphQL实现了GraphQL-Engine和GraphQL-Provider的分布式部署，所以在各个节点的状态需要实现同步。Duo-GraphQL使用了redis订阅/发布的方式实现了状态的同步，详见：《[订阅：Subscription](./subscrription.md)》



Duo-GraphQL的分布式系统实现引擎与供应商的两部分状态同步：

**GraphQL-Engine**：引擎是有状态的，主要就是Schema。即有个GraphQL-Provider有更新时，需要同步各GraphQL-Engine节点的Schema，必须保持一致。

**GraphQL-Provider**：此部分最理想的情况是实现无状态化。但在有状态的服务里面，也是依赖Reids的订阅/发布功能实现。具体就是某个有状态的GraphQL-Provider共同订阅了Redis中的某个节点，当节点有变更时同步相关的状态。此服务的每个节点有关此数据的变更时，发布变更的信息。变更信息体没有做强制性要结构要求，但是基本的原则是：

1. 变更信息必须带变更ID，以便在订阅时做重复订阅处理。重复的变更应该做相应的处理
2. 所有订阅必须做重复处理。即是接收到相同变更ID时，应该做特殊处理

