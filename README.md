项目说明

1.使用quartz的job实现异步任务

2.在job的基础之上重新包装,增加自己的基础设置，比如讲每次的异步任务执行情况，执行数据记录自己的数据库；

异步任务设计亮点：
1.客户端异步任务实现类必须继承自己设计的UserJobExecutor，其内部实现方式是一个默认的job实现类即：DefaultJobImpl，但其依旧是实现quartz的job类
这样的设计亮点是对客户端隐藏job的代码，客户端没法感知job的存在；
2.DefaultJobImpl执行体通过发射机制加载客户端异步任务的真正执行类，该任务执行类在第一步已经说过，是UserJobExecutor的实现类，反射得到执行类后执行
默认的方法：execute方法，所以也就规定了在进行任务触发的时候，入参jobClass必须继承自UserJobExecutor的一个class；
3.异步任务的执行方法规定了返回的结果为：UserJobExecuteResult，这样对所有异步任务都有课共同的规约，返回的都是同样的数据，方便管理，而其内部又嵌套
了jsonObject对象，又不限制了用户需要数据，用户可以随意返回自己的数据，所以即约束科用户的返回结果，但是也给用户留下扩展的空间；其次，内部封装的服务在
调用该异步任务的执行方法后，亦将执行结果持久化到mongo数据库，保存用户的数据，也方便回头看；

