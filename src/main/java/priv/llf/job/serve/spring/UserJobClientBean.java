package priv.llf.job.serve.spring;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.RemoteScheduler;
import org.quartz.impl.SchedulerRepository;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.quartz.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import priv.llf.job.client.service.UserJobClient;
import priv.llf.job.serve.UserSchedulerClientService;
import priv.llf.job.serve.impl.DefaultUserJobClient;
import priv.llf.job.serve.impl.UserSchedulerClientServiceImpl;
import priv.llf.job.support.UserJobConstants;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @Author: Eleven
 * @Since: 2018/3/30 21:37
 * @Description:
 */
@Slf4j
public class UserJobClientBean extends SchedulerAccessor implements FactoryBean<UserJobClient>,
        BeanNameAware, ApplicationContextAware, InitializingBean, DisposableBean, SmartLifecycle {

    private static final ThreadLocal<ResourceLoader> configTimeResourceLoaderHolder =
            new ThreadLocal<ResourceLoader>();

    private static final ThreadLocal<Executor> configTimeTaskExecutorHolder =
            new ThreadLocal<Executor>();

    private static final ThreadLocal<DataSource> configTimeDataSourceHolder =
            new ThreadLocal<DataSource>();

    private static final ThreadLocal<DataSource> configTimeNonTransactionalDataSourceHolder =
            new ThreadLocal<DataSource>();


    private Class<? extends SchedulerFactory> schedulerFactoryClass = StdSchedulerFactory.class;

    private String schedulerName;

    private Resource configLocation;

    private Properties quartzProperties;

    /**
     * 调度器实例id,不填就是AUTO
     */
    @Setter
    protected String name;
    private Executor taskExecutor;

    private DataSource dataSource;

    private DataSource nonTransactionalDataSource;


    private Map<String, ?> schedulerContextMap;

    private ApplicationContext applicationContext;

    private String applicationContextSchedulerContextKey;

    private JobFactory jobFactory;

    private boolean jobFactorySet = false;


    private boolean autoStartup = true;

    private int startupDelay = 0;

    private int phase = Integer.MAX_VALUE;

    private boolean exposeSchedulerInRepository = false;

    private boolean waitForJobsToCompleteOnShutdown = false;


    private Scheduler scheduler;

    private UserSchedulerClientService userSchedulerClientService;

    private UserJobClient client;

    protected ServiceConfig<UserSchedulerClientService> schedulerClientServiceConfig;

    protected ApplicationConfig ac=new ApplicationConfig(UserJobConstants.applicationName);

    protected ProtocolConfig protocol;

    protected RegistryConfig zkRegistryConfig=new RegistryConfig();
    @Setter
    protected String zookeeperAddress;
    @Setter
    protected ProviderConfig provider;
    /**
     * Set the Quartz SchedulerFactory implementation to use.
     * <p>Default is {@link StdSchedulerFactory}, reading in the standard
     * {@code quartz.properties} from {@code quartz.jar}.
     * To use custom Quartz properties, specify the "configLocation"
     * or "quartzProperties" bean property on this FactoryBean.
     * @see org.quartz.impl.StdSchedulerFactory
     * @see #setConfigLocation
     * @see #setQuartzProperties
     */
    public void setSchedulerFactoryClass(Class<? extends SchedulerFactory> schedulerFactoryClass) {
        Assert.isAssignable(SchedulerFactory.class, schedulerFactoryClass);
        this.schedulerFactoryClass = schedulerFactoryClass;
    }

    /**
     * Set the name of the Scheduler to create via the SchedulerFactory.
     * <p>If not specified, the bean name will be used as default scheduler name.
     * @see #setBeanName
     * @see org.quartz.SchedulerFactory#getScheduler()
     * @see org.quartz.SchedulerFactory#getScheduler(String)
     */
    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    /**
     * Set the location of the Quartz properties config file, for example
     * as classpath resource "classpath:quartz.properties".
     * <p>Note: Can be omitted when all necessary properties are specified
     * locally via this bean, or when relying on Quartz' default configuration.
     * @see #setQuartzProperties
     */
    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    /**
     * Set Quartz properties, like "org.quartz.threadPool.class".
     * <p>Can be used to override values in a Quartz properties config file,
     * or to specify all necessary properties locally.
     * @see #setConfigLocation
     */
    public void setQuartzProperties(Properties quartzProperties) {
        this.quartzProperties = quartzProperties;
    }


    /**
     * Set the Spring TaskExecutor to use as Quartz backend.
     * Exposed as thread pool through the Quartz SPI.
     * <p>Can be used to assign a JDK 1.5 ThreadPoolExecutor or a CommonJ
     * WorkManager as Quartz backend, to avoid Quartz's manual thread creation.
     * <p>By default, a Quartz SimpleThreadPool will be used, configured through
     * the corresponding Quartz properties.
     * @see #setQuartzProperties
     * @see LocalTaskExecutorThreadPool
     * @see org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
     * @see org.springframework.scheduling.commonj.WorkManagerTaskExecutor
     */
    public void setTaskExecutor(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * Set the default DataSource to be used by the Scheduler. If set,
     * this will override corresponding settings in Quartz properties.
     * <p>Note: If this is set, the Quartz settings should not define
     * a job store "dataSource" to avoid meaningless double configuration.
     * <p>A Spring-specific subclass of Quartz' JobStoreCMT will be used.
     * It is therefore strongly recommended to perform all operations on
     * the Scheduler within Spring-managed (or plain JTA) transactions.
     * Else, database locking will not properly work and might even break
     * (e.g. if trying to obtain a lock on Oracle without a transaction).
     * <p>Supports both transactional and non-transactional DataSource access.
     * With a non-XA DataSource and local Spring transactions, a single DataSource
     * argument is sufficient. In case of an XA DataSource and global JTA transactions,
     * SchedulerFactoryBean's "nonTransactionalDataSource" property should be set,
     * passing in a non-XA DataSource that will not participate in global transactions.
     * @see #setNonTransactionalDataSource
     * @see #setQuartzProperties
     * @see #setTransactionManager
     * @see LocalDataSourceJobStore
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Set the DataSource to be used by the Scheduler <i>for non-transactional access</i>.
     * <p>This is only necessary if the default DataSource is an XA DataSource that will
     * always participate in transactions: A non-XA version of that DataSource should
     * be specified as "nonTransactionalDataSource" in such a scenario.
     * <p>This is not relevant with a local DataSource instance and Spring transactions.
     * Specifying a single default DataSource as "dataSource" is sufficient there.
     * @see #setDataSource
     * @see LocalDataSourceJobStore
     */
    public void setNonTransactionalDataSource(DataSource nonTransactionalDataSource) {
        this.nonTransactionalDataSource = nonTransactionalDataSource;
    }


    /**
     * Register objects in the Scheduler context via a given Map.
     * These objects will be available to any Job that runs in this Scheduler.
     * <p>Note: When using persistent Jobs whose JobDetail will be kept in the
     * database, do not put Spring-managed beans or an ApplicationContext
     * reference into the JobDataMap but rather into the SchedulerContext.
     * @param schedulerContextAsMap Map with String keys and any objects as
     * values (for example Spring-managed beans)
     * @see JobDetailFactoryBean#setJobDataAsMap
     */
    public void setSchedulerContextAsMap(Map<String, ?> schedulerContextAsMap) {
        this.schedulerContextMap = schedulerContextAsMap;
    }

    /**
     * Set the key of an ApplicationContext reference to expose in the
     * SchedulerContext, for example "applicationContext". Default is none.
     * Only applicable when running in a Spring ApplicationContext.
     * <p>Note: When using persistent Jobs whose JobDetail will be kept in the
     * database, do not put an ApplicationContext reference into the JobDataMap
     * but rather into the SchedulerContext.
     * <p>In case of a QuartzJobBean, the reference will be applied to the Job
     * instance as bean property. An "applicationContext" attribute will
     * correspond to a "setApplicationContext" method in that scenario.
     * <p>Note that BeanFactory callback interfaces like ApplicationContextAware
     * are not automatically applied to Quartz Job instances, because Quartz
     * itself is responsible for the lifecycle of its Jobs.
     * @see JobDetailFactoryBean#setApplicationContextJobDataKey
     * @see org.springframework.context.ApplicationContext
     */
    public void setApplicationContextSchedulerContextKey(String applicationContextSchedulerContextKey) {
        this.applicationContextSchedulerContextKey = applicationContextSchedulerContextKey;
    }

    /**
     * Set the Quartz JobFactory to use for this Scheduler.
     * <p>Default is Spring's {@link AdaptableJobFactory}, which supports
     * {@link java.lang.Runnable} objects as well as standard Quartz
     * {@link org.quartz.Job} instances. Note that this default only applies
     * to a <i>local</i> Scheduler, not to a RemoteScheduler (where setting
     * a custom JobFactory is not supported by Quartz).
     * <p>Specify an instance of Spring's {@link SpringBeanJobFactory} here
     * (typically as an inner bean definition) to automatically populate a job's
     * bean properties from the specified job data map and scheduler context.
     * @see AdaptableJobFactory
     * @see SpringBeanJobFactory
     */
    public void setJobFactory(JobFactory jobFactory) {
        this.jobFactory = jobFactory;
        this.jobFactorySet = true;
    }


    /**
     * Set whether to automatically start the scheduler after initialization.
     * <p>Default is "true"; set this to "false" to allow for manual startup.
     */
    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    /**
     * Return whether this scheduler is configured for auto-startup. If "true",
     * the scheduler will start after the context is refreshed and after the
     * start delay, if any.
     */
    public boolean isAutoStartup() {
        return this.autoStartup;
    }

    /**
     * Specify the phase in which this scheduler should be started and
     * stopped. The startup order proceeds from lowest to highest, and
     * the shutdown order is the reverse of that. By default this value
     * is Integer.MAX_VALUE meaning that this scheduler starts as late
     * as possible and stops as soon as possible.
     */
    public void setPhase(int phase) {
        this.phase = phase;
    }

    /**
     * Return the phase in which this scheduler will be started and stopped.
     */
    public int getPhase() {
        return this.phase;
    }

    /**
     * Set the number of seconds to wait after initialization before
     * starting the scheduler asynchronously. Default is 0, meaning
     * immediate synchronous startup on initialization of this bean.
     * <p>Setting this to 10 or 20 seconds makes sense if no jobs
     * should be run before the entire application has started up.
     */
    public void setStartupDelay(int startupDelay) {
        this.startupDelay = startupDelay;
    }

    /**
     * Set whether to expose the Spring-managed {@link Scheduler} instance in the
     * Quartz {@link SchedulerRepository}. Default is "false", since the Spring-managed
     * Scheduler is usually exclusively intended for access within the Spring context.
     * <p>Switch this flag to "true" in order to expose the Scheduler globally.
     * This is not recommended unless you have an existing Spring application that
     * relies on this behavior. Note that such global exposure was the accidental
     * default in earlier Spring versions; this has been fixed as of Spring 2.5.6.
     */
    public void setExposeSchedulerInRepository(boolean exposeSchedulerInRepository) {
        this.exposeSchedulerInRepository = exposeSchedulerInRepository;
    }

    /**
     * Set whether to wait for running jobs to complete on shutdown.
     * <p>Default is "false". Switch this to "true" if you prefer
     * fully completed jobs at the expense of a longer shutdown phase.
     * @see org.quartz.Scheduler#shutdown(boolean)
     */
    public void setWaitForJobsToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
        this.waitForJobsToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
    }


    public void setBeanName(String name) {
        if (this.schedulerName == null) {
            this.schedulerName = name;
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    //---------------------------------------------------------------------
    // Implementation of InitializingBean interface
    //---------------------------------------------------------------------

    public void afterPropertiesSet() throws Exception {
        if (this.dataSource == null && this.nonTransactionalDataSource != null) {
            this.dataSource = this.nonTransactionalDataSource;
        }

        if (this.applicationContext != null && this.resourceLoader == null) {
            this.resourceLoader = this.applicationContext;
        }

        // Create SchedulerFactory instance...
        SchedulerFactory schedulerFactory = BeanUtils.instantiateClass(this.schedulerFactoryClass);
        initSchedulerFactory(schedulerFactory);

        if (this.resourceLoader != null) {
            // Make given ResourceLoader available for SchedulerFactory configuration.
            configTimeResourceLoaderHolder.set(this.resourceLoader);
        }
        if (this.taskExecutor != null) {
            // Make given TaskExecutor available for SchedulerFactory configuration.
            configTimeTaskExecutorHolder.set(this.taskExecutor);
        }
        if (this.dataSource != null) {
            // Make given DataSource available for SchedulerFactory configuration.
            configTimeDataSourceHolder.set(this.dataSource);
        }
        if (this.nonTransactionalDataSource != null) {
            // Make given non-transactional DataSource available for SchedulerFactory configuration.
            configTimeNonTransactionalDataSourceHolder.set(this.nonTransactionalDataSource);
        }

        // Get Scheduler instance from SchedulerFactory.
        try {
            initScheduler();//初始化schedule
            populateSchedulerContext();

            if (!this.jobFactorySet && !(this.scheduler instanceof RemoteScheduler)) {
                // Use AdaptableJobFactory as default for a local Scheduler, unless when
                // explicitly given a null value through the "jobFactory" bean property.
                this.jobFactory = new AdaptableJobFactory();
            }
            if (this.jobFactory != null) {
                if (this.jobFactory instanceof SchedulerContextAware) {
                    ((SchedulerContextAware) this.jobFactory).setSchedulerContext(this.scheduler.getContext());
                }
                this.scheduler.setJobFactory(this.jobFactory);
            }
        }

        finally {
            if (this.resourceLoader != null) {
                configTimeResourceLoaderHolder.remove();
            }
            if (this.taskExecutor != null) {
                configTimeTaskExecutorHolder.remove();
            }
            if (this.dataSource != null) {
                configTimeDataSourceHolder.remove();
            }
            if (this.nonTransactionalDataSource != null) {
                configTimeNonTransactionalDataSourceHolder.remove();
            }
        }
        initProtocol();
        initProvider();
        initZkRegistry();
        registerListeners();
        registerJobsAndTriggers();
        initUserSchedulerClientService();////初始化客户端的调度器操作服务
        initUserJobClient();   //初始化用户的异步任务执行客户端
        publishSchedulerClientService();// //发布客户端的调度器操作服务
        //该步骤将schedule调度器注册到spring，保证分布式系统中schedule只有一个对象，保证同步
//        registerScheduleBean("userSchedulerClientServiceImpl",UserSchedulerClientServiceImpl.class.getName(),map);
    }
    /**
     * 初始化schedule
     * @throws Exception
     */
    protected void initScheduler() throws Exception {
        SchedulerFactoryBean sfb=new SchedulerFactoryBean();
        sfb.setApplicationContext(applicationContext);
        sfb.setDataSource(dataSource);
        sfb.setTaskExecutor(taskExecutor);
        sfb.setSchedulerName(schedulerName);
        sfb.setQuartzProperties(quartzProperties);
        sfb.afterPropertiesSet();
        scheduler=sfb.getScheduler();
        if(StdSchedulerFactory.AUTO_GENERATE_INSTANCE_ID.equalsIgnoreCase(name)){
            String instanceId=scheduler.getSchedulerInstanceId();
            String userDir=System.getProperty("user.dir");
            File file = new File(userDir+"/"+schedulerName+".userJob.properties");
            Properties props=new Properties();
            props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, instanceId);
            BufferedWriter writer=new BufferedWriter(new FileWriter(file));
            try {
                props.store(writer, null);
                log.info("由于配置使用了AUTO生成异步任务调度器实例id现将生成的实例id["+instanceId+"]存储到"+file);
            } finally {
                IOUtils.closeQuietly(writer);
            }
        }
        scheduler.start();
        //将上下文注册到调度器的上下文
        scheduler.getContext().put(UserJobConstants.applicationContextKey, applicationContext);
    }
    /**
     * 将一些额外需要数据的填充到schedule的上下文中
     * 如果没有，就不填充
     */
    private void populateSchedulerContext() throws SchedulerException {
        if (this.schedulerContextMap != null) {
            this.scheduler.getContext().putAll(this.schedulerContextMap);
        }
    }

    /**
     * //初始化客户端的调度器操作服务
     */
    protected void initUserSchedulerClientService(){
        AutowireCapableBeanFactory beanFactory=applicationContext.getAutowireCapableBeanFactory();
        Object bean=beanFactory.createBean(UserSchedulerClientServiceImpl.class,AutowireCapableBeanFactory.AUTOWIRE_NO,false);
        UserSchedulerClientServiceImpl impl=(UserSchedulerClientServiceImpl)bean;
        impl.setScheduler(scheduler);
        userSchedulerClientService =impl;
    }
    //发布客户端的调度器操作服务
    protected void publishSchedulerClientService() throws SchedulerException {
        String schedulerCluster=scheduler.getSchedulerName();
        schedulerClientServiceConfig=new ServiceConfig<UserSchedulerClientService>();
        schedulerClientServiceConfig.setInterface(UserSchedulerClientService.class);
        schedulerClientServiceConfig.setApplication(ac);
        schedulerClientServiceConfig.setProtocol(protocol);
        schedulerClientServiceConfig.setRegistry(zkRegistryConfig);
        schedulerClientServiceConfig.setProvider(provider);
        schedulerClientServiceConfig.setGroup(schedulerCluster);
        schedulerClientServiceConfig.setRef(userSchedulerClientService);
        Map<String,String> parameters=new HashMap<String,String>();
        parameters.put(UserJobConstants.schedulerNameKey,scheduler.getSchedulerInstanceId());
        parameters.put(UserJobConstants.schedulerClusterKey,schedulerCluster);
        schedulerClientServiceConfig.setParameters(parameters);
        schedulerClientServiceConfig.export();
    }
    /**
     * 初始化执行用户异步任务的userJobClient
     */
    protected void initUserJobClient() throws Exception{
        client=new DefaultUserJobClient(scheduler, userSchedulerClientService);
    }
    //初始化dubbo协议
    protected void initProtocol(){
        if(protocol==null){
            int port= NetUtils.getAvailablePort(DubboProtocol.DEFAULT_PORT);
            protocol=new ProtocolConfig(DubboProtocol.NAME,port);
        }
    }
    //初始化zk注册中心
    protected void initZkRegistry(){
        Map<String,RegistryConfig> rcMap=applicationContext.getBeansOfType(RegistryConfig.class);
        zkRegistryConfig.setProtocol(UserJobConstants.registryProtocol);
        zkRegistryConfig.setGroup(UserJobConstants.userJobGroup);
        if(MapUtils.isNotEmpty(rcMap)){//如果有dubbo的zk的注册中心配置直接使用
            for(Map.Entry<String, RegistryConfig> entry:rcMap.entrySet()){
                RegistryConfig rc=entry.getValue();
                String protocol=rc.getProtocol();
                String address=rc.getAddress();
                if(UserJobConstants.registryProtocol.equalsIgnoreCase(protocol)){
                    zkRegistryConfig.setAddress(address);
                    break;
                }else if(address.startsWith("zookeeper")){
                    zkRegistryConfig.setAddress(address.substring(address.indexOf("//")+2));
                    break;
                }
            }
        }
        if(StringUtils.isBlank(zkRegistryConfig.getAddress())){//没有dubbo的zk注册中心配置时自己根据zookeeperAddress创建RegistryConfig
            Assert.hasText(zookeeperAddress,"zookeeperAddress为空无法向zookeeper的注册开放的服务");
            zkRegistryConfig.setAddress(zookeeperAddress);
        }
        zookeeperAddress=zkRegistryConfig.getAddress();
        String[] zkAddresses=zookeeperAddress.split(",");
        zookeeperAddress="";
        for(String address:zkAddresses){
            URL url=URL.valueOf(address);
            List<URL> backupUrls=url.getBackupUrls();
            for(URL backupUrl:backupUrls){
                if(StringUtils.isBlank(zookeeperAddress)){
                    zookeeperAddress+=backupUrl.getAddress();
                }else{
                    zookeeperAddress+=","+backupUrl.getAddress();
                }
            }
        }
        zkRegistryConfig.setAddress(zookeeperAddress);
    }
    //初始化zk注册中心
    private void initProvider(){
        if (provider ==null){
            provider = new ProviderConfig();
            provider.setId("asynchronousJob_provider");
        }
    }
    /**
     * 将初始化完成的schedule注册都spring IOC容器,供其它service注入
     * @param beanId   注册到spring IOC的beanID，自己定义
     * @param className  待注册的bean的class name，可以直接以xxx.class.getName()获取
     * @param propertyMap  待注册的bean的属性输入值，以key-value的形式注入
     */
    public void registerScheduleBean(String beanId,String className,Map propertyMap) {
        // get the BeanDefinitionBuilder
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;
        BeanDefinitionRegistry beanDefinitionRegistry = (DefaultListableBeanFactory) configurableContext.getBeanFactory();

        BeanDefinitionBuilder beanDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(className);
        if(propertyMap!=null){
            Iterator<?> entries = propertyMap.entrySet().iterator();
            Map.Entry<?, ?> entry;
            while (entries.hasNext()) {
                entry = (Map.Entry<?, ?>) entries.next();
                String key = (String) entry.getKey();
                Object val =  entry.getValue();
                beanDefinitionBuilder.addPropertyValue(key, val);
            }
        }
        BeanDefinition beanDefinition=beanDefinitionBuilder.getBeanDefinition();
        beanDefinitionRegistry.registerBeanDefinition(beanId,beanDefinition);
    }
    /**
     * Load and/or apply Quartz properties to the given SchedulerFactory.
     * @param schedulerFactory the SchedulerFactory to initialize
     */
    private void initSchedulerFactory(SchedulerFactory schedulerFactory) throws SchedulerException, IOException {
        if (!(schedulerFactory instanceof StdSchedulerFactory)) {
            if (this.configLocation != null || this.quartzProperties != null ||
                    this.taskExecutor != null || this.dataSource != null) {
                throw new IllegalArgumentException(
                        "StdSchedulerFactory required for applying Quartz properties: " + schedulerFactory);
            }
            // Otherwise assume that no initialization is necessary...
            return;
        }

        Properties mergedProps = new Properties();

        if (this.resourceLoader != null) {
            mergedProps.setProperty(StdSchedulerFactory.PROP_SCHED_CLASS_LOAD_HELPER_CLASS,
                    ResourceLoaderClassLoadHelper.class.getName());
        }

        if (this.taskExecutor != null) {
            mergedProps.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS,
                    LocalTaskExecutorThreadPool.class.getName());
        }
        else {
            // Set necessary default properties here, as Quartz will not apply
            // its default configuration when explicitly given properties.
            mergedProps.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, SimpleThreadPool.class.getName());
            mergedProps.setProperty(UserJobConstants.PROP_THREAD_COUNT, Integer.toString(UserJobConstants.DEFAULT_THREAD_COUNT));
        }

        if (this.configLocation != null) {
            if (logger.isInfoEnabled()) {
                logger.info("Loading Quartz config from [" + this.configLocation + "]");
            }
            PropertiesLoaderUtils.fillProperties(mergedProps, this.configLocation);
        }

        CollectionUtils.mergePropertiesIntoMap(this.quartzProperties, mergedProps);

        if (this.dataSource != null) {
            mergedProps.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, LocalDataSourceJobStore.class.getName());
        }

        // Make sure to set the scheduler name as configured in the Spring configuration.
        if (this.schedulerName != null) {
            mergedProps.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, this.schedulerName);
        }

        ((StdSchedulerFactory) schedulerFactory).initialize(mergedProps);
    }


    /**
     * Start the Quartz Scheduler, respecting the "startupDelay" setting.
     * @param scheduler the Scheduler to start
     * @param startupDelay the number of seconds to wait before starting
     * the Scheduler asynchronously
     */
    protected void startScheduler(final Scheduler scheduler, final int startupDelay) throws SchedulerException {
        if (startupDelay <= 0) {
            logger.info("Starting Quartz Scheduler now");
            scheduler.start();
        }
        else {
            if (logger.isInfoEnabled()) {
                logger.info("Will start Quartz Scheduler [" + scheduler.getSchedulerName() +
                        "] in " + startupDelay + " seconds");
            }
            Thread schedulerThread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(startupDelay * 1000);
                    }
                    catch (InterruptedException ex) {
                        // simply proceed
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info("Starting Quartz Scheduler now, after delay of " + startupDelay + " seconds");
                    }
                    try {
                        scheduler.start();
                    }
                    catch (SchedulerException ex) {
                        throw new SchedulingException("Could not start Quartz Scheduler after delay", ex);
                    }
                }
            };
            schedulerThread.setName("Quartz Scheduler [" + scheduler.getSchedulerName() + "]");
            schedulerThread.setDaemon(true);
            schedulerThread.start();
        }
    }


    //---------------------------------------------------------------------
    // Implementation of FactoryBean interface
    //---------------------------------------------------------------------

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public UserJobClient getObject() {
        return this.client;
    }

    public Class<? extends UserJobClient> getObjectType() {
        return (this.client != null) ? this.client.getClass() : UserJobClient.class;
    }

    public boolean isSingleton() {
        return true;
    }


    //---------------------------------------------------------------------
    // Implementation of SmartLifecycle interface
    //---------------------------------------------------------------------

    public void start() throws SchedulingException {
        if (this.scheduler != null) {
            try {
                startScheduler(this.scheduler, this.startupDelay);
            }
            catch (SchedulerException ex) {
                throw new SchedulingException("Could not start Quartz Scheduler", ex);
            }
        }
    }

    public void stop() throws SchedulingException {
        if (this.scheduler != null) {
            try {
                this.scheduler.standby();
            }
            catch (SchedulerException ex) {
                throw new SchedulingException("Could not stop Quartz Scheduler", ex);
            }
        }
    }

    public void stop(Runnable callback) throws SchedulingException {
        stop();
        callback.run();
    }

    public boolean isRunning() throws SchedulingException {
        if (this.scheduler != null) {
            try {
                return !this.scheduler.isInStandbyMode();
            }
            catch (SchedulerException ex) {
                return false;
            }
        }
        return false;
    }


    //---------------------------------------------------------------------
    // Implementation of DisposableBean interface
    //---------------------------------------------------------------------

    /**
     * Shut down the Quartz scheduler on bean factory shutdown,
     * stopping all scheduled jobs.
     */
    public void destroy() throws SchedulerException {
        logger.info("Shutting down Quartz Scheduler");
        this.scheduler.shutdown(this.waitForJobsToCompleteOnShutdown);
    }
}
