#CurrencyThreadTask

* 这个库封装线程控制(线程池，内置切换任务)，线程通信功能和通用循环任务事务功能，生产消费任务等


```
    循环任务事务生命周期 

    /**
     * 初始化任务
     */
    protected void onInitTask() {
        //Do something
    }

    /**
     * 循环执行
     */
    protected void onRunLoopTask() {
        //Do something
    }

    /**
     * 任务进入懒关闭状态，处理完该回调即将调用onDestroyTask
     */
    protected void onIdleStop() {
        //Do something
    }

    /**
     * 任务销毁
     */
    protected void onDestroyTask() {
        //Do something
    }

    列子：
    BaseLoopTask task = new BaseLoopTask();
    TaskContainer contatiner = new TaskContainer(task);
    ILoopTaskExecutor executor = contatiner.getTaskExecutor();
    executor.startTask();
```

[![](https://jitpack.io/v/Yyz-Conan/CurrencyThreadTask.svg)](https://jitpack.io/#Yyz-Conan/CurrencyThreadTask)
