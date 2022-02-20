This repository contains self contained project samples showing how kotlin (written in a functional style) can be used along with project reactor to take advantage of existing java infrastructure in various scenarios. After experimenting with these projects, it seems to me like reactor can be used as a backbone for writing various different services in kotlin due to it being very wide spread, well supported and already intergrated with various libraries (e.g. the various reactive spring dependencies).

The samples are:
* `reactor-rabbitmq`, containing a small service showing how one can use reactor-rabbit with kotlin to consume from and publish to rabbitmq queues. This service uses flows to interface with the reactor publishers.

* `spring-reactor-api` containing a small api connected to a database using spring libraries (spring-r2dbc, spring-webflux). The spring dependencies are used outside the springboot runtime and thus there isnt an ioc container for dependency injection. Using reactor as a backbone, one can effectively and easily use the reactive spring dependencies on their own. This takes out a lot of the spring "magic", making the codebase more functional and, from my observations, more appropriate for smaller services.

maybe someone finds this useful :)
