package com.home.common

object Constants {


  /** 根目录，也可通过 -D 传入参数的方式获取 */
  val ROOT = RootClassPath.getRootClassPath()
  /** 相对于根目录父module */
  val APP_CONF_PATH = s"${ROOT}conf/app.conf"
  val APP_TEST_PATH = s"${ROOT}conf/test.conf"

  val appConf = Config(APP_CONF_PATH)

  val schema =
    """
      |
      |//环上数据传递的schema
      |message SpiderPack {
      |    optional Link link = 1;
      |    optional Page page = 2;
      |    optional uint64 logid = 3;
      |}
      |
      |//链接库数据结构
      |message Link {
      |    optional bytes        url = 1;              //url 不带http://等协议头
      |    optional HttpProtocol http_protocol = 2;    //HTTP/HTTPS
      |    optional bytes        exprotocol = 3;       //ua=wise
      |
      |    optional uint64       url_sign = 4;         //url签名
      |    optional uint64       site_sign = 5;        //site签名
      |    optional uint64       domain_sign = 6;      //domain签名
      |
      |    optional int32        first_in_time = 7;    //首次进入link库时间
      |    optional int32        last_in_time = 8;     //最近入库时间
      |    optional bool         is_crawled = 9;       //是否被抓取过
      |    optional bool         is_hub = 10;          //是否是索引页
      |
      |    optional bytes        anchor = 11;          //(Anchor最大长度为64 bytes)
      |    optional bytes        preurl = 12;          //前链url
      |    optional bool         preurl_is_hub = 13;   //前链是否是索引页
      |    optional uint32       preurl_depth = 14;    //记录前链的链接深度
      |    optional uint32       preurl_level = 15;    //url的前链价值
      |    optional LinkSrc      link_src = 16;        //链接的来源属性
      |
      |    optional UrlCrawl     crawl = 17;           //调度抓取相关的属性
      |    optional UrlFeature   feature = 18;         //页面特征属性
      |}
      |
      |message UrlCrawl {//调度抓取相关的属性
      |    optional int32    last_mod_time = 1;        //上次修改时间，由http服务器给出
      |    optional int32    last_crawl_time = 2;      //latest crawling time
      |    optional uint32    ip = 3;                  //ip
      |    optional CrawlResult crawl_result = 4;      //抓取结果
      |    optional bool modified = 5;                 //页面是否有变化 针对CHECK请求
      |    optional int32 http_code = 6;               //http返回码
      |
      |    //etag，HTTP协议中的一个标识，通过这个标识可以知道页面是否更改(具体见HTTP协议)。etag最长为64个byte
      |    optional bytes    etag = 7;
      |
      |    optional uint32    not_mod_times = 8;       //Not Mod times.（页面内容与上次CHK或GET到的相比并未发生改变的次数）
      |    optional uint32    fail_times = 9;          //CHK或GET失败次数
      |    optional bool      is_select = 10;           //选取位（true标识该链接已被select选出，且还没有被saver merge回linkbase）
      |    optional uint32    select_time = 11;        //该URL最近一次被select模块选取的时间
      |    optional int32     priority = 12;          //调度优先级 0-999
      |    optional CrawlType crawl_type = 13;         //抓取类型，RAW：普通抓取;RENDER：渲染抓取
      |    optional HttpVersion http_version = 14;     //V10：http 1.0;V11：http 1.1
      |
      |    optional uint32    expect_period = 15;  //抓取周期，单位秒
      |    optional uint32    expect_idx = 16;     //抓取周期拉长到几个周期
      |
      |    optional RedirType redir_type = 17;     //重定向类型
      |
      |    optional uint32    link_depth = 18;     //the link depth of the URL.（表示从首页到该页的深度）
      |    optional uint32    dir_depth = 19;      //the length of the directory.（URL的目录深度）
      |
      |    optional UrlType   url_type = 20;       //the type of the url.(URL类型，0为静态网页，1为动态网页)
      |    optional uint32    url_level = 21;      //the level of the url page.（人工设置的URL级别，由modlink更新该值）
      |
      |    optional bytes     redir_url = 22;      //重定向到的最终url
      |    optional uint32    render_http_code = 23; //渲染http返回码
      |    optional uint64    render_cost_time_ms = 24;   // 渲染耗时ms
      |    optional bytes     action_script = 25;       // 动作脚本，用于在渲染加载完成后执行，主要用于下拉或模拟点击
      |    optional bytes     referrer = 26;            // 渲染网页使用的 referrer，必须是带协议头的完整url
      |    optional RenderResult render_result = 27;      //渲染结果
      |}
      |message UrlFeature {//页面特征属性
      |    optional int32    page_length = 1;    //网页长度(页面的长度，根据HTTP从server上得到)
      |    optional uint32   extern_link = 2;    //该url的外链数
      |    optional int32    publish_time = 3;   //网页发布时间，页面解析提取得到
      |
      |    optional uint64      long_sign = 4;       //最长句子签名
      |
      |    optional LangType    lang_type = 5;       //语言类型
      |    optional PageType    page_type = 6;       //the type of the page.(内容型，索引型)
      |    optional CodeType    code_type = 7;       //网页内容编码，EN,GB,BIG5(网页内容编码，分为GB、BIG5、ENG等)
      |    optional uint64      simhash = 8;
      |    optional uint32      pagerank = 9;
      |    optional PageClass   page_class = 10;     //页面分类，详见枚举类型定义
      |    optional DocType     doc_type = 11;       //文档类型，img,pdf,doc
      |    optional uint32      level = 12;          //页面级别(如果该URL未抓过，则level为0)
      |    optional uint32      authority = 13;      //页面权威性
      |    optional uint32      title_length = 14;   //标题长度
      |    optional uint32      content_length = 15; //正文内容长度
      |    optional uint32      central_length = 16; //核心正文长度
      |    optional int32       fresh_value = 17;    //链接的时效性价值
      |    optional int32       sex_factor = 18;     //色情系数
      |    optional int32       political_factor = 19;//政治系数
      |    optional uint32      wise_level = 20;     //wise页级别
      |}
      |
      |message Page {//页面相关内容（网页库），不与上述字段重合
      |    optional bytes    title = 1;         //网页TITLE字段
      |    optional bytes    real_title = 2;    //网页真实标题，页面分析提取所得
      |    optional bytes    mypos = 3;         //页面位置，页面分析提取所得
      |    optional bytes    html = 4;          //页面原始html
      |    optional bytes    content = 5;       //页面文本内容，去除html标签后的内容
      |    optional bytes    meta = 6;          //页面meta信息
      |    repeated SimpleLink out_links = 7;   //出链，url+anchor
      |
      |    //url 同message Link url内容一致，单独扫page列的时候用，避免还要查询link列
      |    optional bytes    url = 8;
      |    optional bytes    render_html_body = 9; //渲染后的html页面
      |    optional bool     html_body_truncated = 10; // 页面是否根据渲染输入的 max_html_body_size 被截断了
      |    repeated MultiLevelLabel multi_level_label = 11; //多级标签
      |}
      |
      |message SimpleLink {//简单链接结构，用于后链
      |    optional bytes url = 1;
      |    optional HttpProtocol http_protocol = 2;    //HTTP/HTTPS
      |    optional bytes anchor = 3;
      |}
      |
      |message MultiLevelLabel{
      |    optional bytes name = 1; //标签名称
      |}
      |
      |enum CrawlResult {//抓取结果
      |    OK = 1;                 //成功抓取
      |    CONNECTION_FAILED = 2;  //连接失败
      |    NO_IP = 3;              //ip不存在，dns查询失败
      |    PROTOCOL_ERROR = 4;     //http/https协议失败,详细查询http_code字段
      |    TIMEOUT = 5;           //抓取请求超时
      |    BODY_TOO_BIG = 6;       //body文件太大，超过配置值
      |    REDIRECT_TOO_MANY = 7;     //重定向次数超过
      |    PARSER_FAILED = 8;      //parser解析失败
      |    CR_UNKNOWN= 100;           //未知的
      |}
      |
      |enum LinkSrc {//链接来源
      |    FOLLOW = 1;    //由其他页面follow所得
      |    BOGUS = 2;    //由直接回灌链接库所得
      |}
      |
      |enum CrawlType {//抓取类型
      |    RAW = 1;    //普通抓取
      |    RENDER = 2;    //渲染抓取
      |}
      |
      |enum HttpVersion {//支持的http版本
      |    V10 = 1;    //http 1.0
      |    V11 = 2;    //http 1.1
      |    V20 = 3;    //http 2.0
      |}
      |
      |enum HttpProtocol {//协议
      |    HTTP = 1;
      |    HTTPS = 2;
      |}
      |
      |enum UrlType {//网页类型
      |    STATIC = 1;    //静态页面
      |    DYNAMIC = 2;//动态页面
      |}
      |
      |enum LangType {//页面语言类型
      |    CN = 1;
      |    EN = 2;
      |}
      |
      |enum PageType {//页面类型
      |    HUB = 1;    //hub页
      |    CONTENT = 2;//内容页
      |}
      |
      |enum CodeType {//编码类型
      |    GBK = 1;
      |    BIG5 = 2;
      |    UTF8 = 3;
      |}
      |
      |enum PageClass {//网页分类
      |    UNKNOWN = 1;     //未知
      |    FORUM = 2;       //论坛页
      |    SOFT = 3;        //软件下载页
      |    STORY = 4;       //小说页
      |    NEWS = 5;        //新闻页
      |    YELLOWPAGE = 6;  //黄页
      |    BLOG = 7;        //博客页
      |    BUSINESS = 8;    //商情页
      |    SEARCHRESULT = 9;//搜索结果页
      |    VIDEO = 10;      //视频页
      |    IMAGE = 11;      //图片页
      |    INDEX = 12;      //索引页
      |}
      |
      |enum DocType {//文档类型
      |    HTML = 1;
      |    IMG = 2;
      |    PDF = 3;
      |    DOC = 4;
      |    PPT = 5;
      |    XLS = 6;
      |    APK = 7;
      |}
      |
      |enum RedirType {//重定向类型，需要看代码再补充
      |    UN = 1;
      |}
      |
      |enum RenderResult {//抓取结果
      |    RENDER_OK = 1;                 //渲染成功
      |    RENDER_TIMEOUT = 2;            //超时
      |    RENDER_UNKNOWN = 100;           //未知的
      |}
      |
    """.stripMargin

}
