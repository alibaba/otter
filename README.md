<h1>
<a name="%E9%A1%B9%E7%9B%AE%E8%83%8C%E6%99%AF" class="anchor" href="#%E9%A1%B9%E7%9B%AE%E8%83%8C%E6%99%AF"><span class="octicon octicon-link"></span></a>项目背景</h1>
<p>
   &nbsp;&nbsp;&nbsp;阿里巴巴B2B公司，因为业务的特性，卖家主要集中在国内，买家主要集中在国外，所以衍生出了杭州和美国异地机房的需求，同时为了提升用户体验，整个机房的架构为双A，两边均可写，由此诞生了otter这样一个产品。 </p>
<p>
   &nbsp;&nbsp;&nbsp;otter第一版本可追溯到04~05年，此次外部开源的版本为第4版，开发时间从2011年7月份一直持续到现在，目前阿里巴巴B2B内部的本地/异地机房的同步需求基本全上了otte4。
</p>
<strong>目前同步规模：</strong>
<ol>
<li>同步数据量6亿</li>
<li>文件同步1.5TB(2000w张图片)</li>
<li>涉及200+个数据库实例之间的同步</li>
<li>80+台机器的集群规模</li>
</ol>
<h1>
<a name="%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D" class="anchor" href="#%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D"><span class="octicon octicon-link"></span></a>项目介绍</h1>
<p>名称：otter ['ɒtə(r)]</p>
<p>译意： 水獭，数据搬运工</p>
<p>语言： 纯java开发</p>
<p>定位： 基于数据库增量日志解析，准实时同步到本机房或异地机房的mysql/oracle数据库. 一个分布式数据库同步系统</p>
<p> </p>
<h1>
<a name="%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86" class="anchor" href="#%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86"><span class="octicon octicon-link"></span></a>工作原理</h1>
<p><img width="848" src="https://camo.githubusercontent.com/2988fbbc7ddfe94ed027cd71720b1ffa5912a635/687474703a2f2f646c322e69746579652e636f6d2f75706c6f61642f6174746163686d656e742f303038382f313138392f64343230636131342d326438302d336435352d383038312d6239303833363036613830312e6a7067" height="303" alt=""></p>
<p>原理描述：</p>
<p>1.   基于Canal开源产品，获取数据库增量日志数据。 什么是Canal,  请<a href="https://github.com/alibaba/canal">点击</a></p>
<p>2.   典型管理系统架构，manager(web管理)+node(工作节点)</p>
<p>       &nbsp;&nbsp;&nbsp; a.  manager运行时推送同步配置到node节点</p>
<p>       &nbsp;&nbsp;&nbsp; b.  node节点将同步状态反馈到manager上</p>
<p>3.  基于zookeeper，解决分布式状态调度的，允许多node节点之间协同工作. </p>
<h3>
<a name="%E4%BB%80%E4%B9%88%E6%98%AFcanal-" class="anchor" href="#%E4%BB%80%E4%B9%88%E6%98%AFcanal-"><span class="octicon octicon-link"></span></a>什么是canal? </h3>
otter之前开源的一个子项目，开源链接地址：<a href="http://github.com/alibaba/canal">http://github.com/alibaba/canal</a>
<p> </p>
<h1>
<a name="introduction" class="anchor" href="#introduction"><span class="octicon octicon-link"></span></a>Introduction</h1>
<p>See the page for introduction: <a class="internal present" href="https://github.com/alibaba/otter/wiki/Introduction">Introduction</a>.</p>
<h1>
<a name="quickstart" class="anchor" href="#quickstart"><span class="octicon octicon-link"></span></a>QuickStart</h1>
<p>See the page for quick start: <a class="internal present" href="https://github.com/alibaba/otter/wiki/QuickStart">QuickStart</a>.</p>
<p> </p>
<h1>
<a name="adminguide" class="anchor" href="#adminguide"><span class="octicon octicon-link"></span></a>AdminGuide</h1>
<p>See the page for admin deploy guide : <a class="internal present" href="https://github.com/alibaba/otter/wiki/Adminguide">AdminGuide</a></p>
<p> </p>
<h1>
<a name="%E7%9B%B8%E5%85%B3%E6%96%87%E6%A1%A3" class="anchor" href="#%E7%9B%B8%E5%85%B3%E6%96%87%E6%A1%A3"><span class="octicon octicon-link"></span></a>相关文档</h1>
<p>See the page for 文档: <a class="internal present" href="https://github.com/alibaba/otter/wiki/%E7%9B%B8%E5%85%B3ppt%26pdf">相关PPT&amp;PDF</a></p>
<p> </p>
<h1>
<a name="%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98" class="anchor" href="#%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98"><span class="octicon octicon-link"></span></a>常见问题</h1>
<p>See the page for FAQ: <a class="internal present" href="https://github.com/alibaba/otter/wiki/Faq">FAQ</a></p>
<p> </p>

<h1>
<a name="%E7%89%88%E6%9C%AC%E7%9B%B8%E5%85%B3-" class="anchor" href="#%E7%89%88%E6%9C%AC%E7%9B%B8%E5%85%B3-"><span class="octicon octicon-link"></span></a>版本相关: </h1>
<p>1. 建议版本：4.2.12  (otter开源版本从内部演变而来，所以初始版本直接从4.x开始) </p>
<p>2. 下载发布包：<a href="https://github.com/alibaba/otter/releases">download </a></p>
<p>3. maven依赖 ： 暂无 </p>

<h1>相关开源</h1>
<ol>
<li>阿里巴巴mysql数据库binlog的增量订阅&消费组件：<a href="http://github.com/alibaba/canal">http://github.com/alibaba/canal</a></li>
<li>阿里巴巴去Oracle数据迁移同步工具(目标支持MySQL/DRDS)：<a href="http://github.com/alibaba/yugong">http://github.com/alibaba/yugong</a></li>
</ol>

<p> </p>
<h1>
<a name="%E9%97%AE%E9%A2%98%E5%8F%8D%E9%A6%88" class="anchor" href="#%E9%97%AE%E9%A2%98%E5%8F%8D%E9%A6%88"><span class="octicon octicon-link"></span></a>问题反馈</h1>
<h3>
<a name="%E6%B3%A8%E6%84%8Fcanalotter-qq%E8%AE%A8%E8%AE%BA%E7%BE%A4%E5%B7%B2%E7%BB%8F%E5%BB%BA%E7%AB%8B%E7%BE%A4%E5%8F%B7161559791-%E6%AC%A2%E8%BF%8E%E5%8A%A0%E5%85%A5%E8%BF%9B%E8%A1%8C%E6%8A%80%E6%9C%AF%E8%AE%A8%E8%AE%BA" class="anchor" href="#%E6%B3%A8%E6%84%8Fcanalotter-qq%E8%AE%A8%E8%AE%BA%E7%BE%A4%E5%B7%B2%E7%BB%8F%E5%BB%BA%E7%AB%8B%E7%BE%A4%E5%8F%B7161559791-%E6%AC%A2%E8%BF%8E%E5%8A%A0%E5%85%A5%E8%BF%9B%E8%A1%8C%E6%8A%80%E6%9C%AF%E8%AE%A8%E8%AE%BA"><span class="octicon octicon-link"></span></a>注意：canal&amp;otter QQ讨论群已经建立，群号：161559791 ，欢迎加入进行技术讨论。</h3>

<p>1.  <span>qq交流群： 161559791</span></p>
<p><span>2.  </span><span>邮件交流： jianghang115@gmail.com</span></p>
<p><span>3.  </span><span>新浪微博： agapple0002</span></p>
<p><span>4.  </span><span>报告issue：</span><a href="https://github.com/alibaba/otter/issues">issues</a></p>
<p> </p>
