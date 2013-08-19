<h1>项目背景</h1>
<p style="font-size: 14px;">
&nbsp;&nbsp;&nbsp;阿里巴巴B2B公司，因为业务的特性，卖家主要集中在国内，买家主要集中在国外，所以衍生出了杭州和美国异地机房的需求，同时为了提升用户体验，整个机房的架构为双A，两边均可写，由此诞生了otter这样一个产品。 </p>
<p style="font-size: 14px;">
&nbsp;&nbsp;&nbsp;otter第一版本可追溯到04~05年，此次外部开源的版本为第4版，开发时间从2011年7月份一直持续到现在，目前阿里巴巴B2B内部的本地/异地机房的同步需求基本全上了otte4。
</p>
<strong>目前同步规模：</strong>
<ol style="font-size: 14px;">
<li>同步数据量6亿</li>
<li>文件同步1.5TB(2000w张图片)</li>
<li>涉及200+个数据库实例之间的同步</li>
<li>80+台机器的集群规模</li>
</ol>

<h1>项目介绍</h1>
<p style="margin-top: 15px; margin-bottom: 15px; color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">名称：otter ['ɒtə(r)]</p>
<p style="margin-top: 15px; margin-bottom: 15px; color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">译意： 水獭，数据搬运工</p>
<p style="margin-top: 15px; margin-bottom: 15px; color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">语言： 纯java开发</p>
<p style="margin-top: 15px; margin-bottom: 15px; color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">定位： 基于数据库增量日志解析，准实时同步到本机房或异地机房的mysql/oracle数据库. </p>
<p> </p>
<h1>工作原理</h1>
<p><img width="848" src="http://dl2.iteye.com/upload/attachment/0088/1189/d420ca14-2d80-3d55-8081-b9083606a801.jpg" height="303" alt=""></p>
<p>原理描述：</p>
<p>1.   基于Canal开源产品，获取数据库增量日志数据。 什么是Canal,  请<a href="https://github.com/alibaba/canal">点击</a></p>
<p>2.   典型管理系统架构，manager(web管理)+node(工作节点)</p>
<p>&nbsp;&nbsp;&nbsp;     a.  manager运行时推送同步配置到node节点</p>
<p>&nbsp;&nbsp;&nbsp;     b.  node节点将同步状态反馈到manager上</p>
<p>3.  基于zookeeper，解决分布式状态调度的，允许多node节点之间协同工作. </p>
<h3>什么是canal? </h3>
otter之前开源的一个子项目，开源链接地址：<a href="http://github.com/alibaba/canal">http://github.com/alibaba/canal</a>
<p> </p>
<h1>Introduction</h1>
<p>See the page for quick start: [[Introduction]].</p>
<h1>QuickStart</h1>
<p>See the page for quick start: [[QuickStart]].</p>
<p> </p>
<h1>AdminGuide</h1>
<p>See the page for admin deploy guide : [[AdminGuide]]</p>
<p> </p>
<h1>相关文档</h1>
<p>See the page for 文档: [[相关PPT&PDF]]</p>
<p> </p>
<h1>常见问题</h1>
<p>See the page for FAQ: [[FAQ]]</p>
<p> </p>
<h1>问题反馈</h1>
<h3>注意：canal&otter QQ讨论群已经建立，群号：161559791 ，欢迎加入进行技术讨论。</h3>

<p>1.  <span style="color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">qq交流群： 161559791</span></p>
<p><span style="color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">2.  </span><span style="color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">邮件交流： jianghang115@gmail.com</span></p>
<p><span style="color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">3.  </span><span style="color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">新浪微博： agapple0002</span></p>
<p><span style="color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">4.  </span><span style="color: #333333; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">报告issue：</span><a href="https://github.com/agapple/otter/issues" style="color: #4183c4; font-family: Helvetica, arial, freesans, clean, sans-serif; font-size: 15px; line-height: 25px;">issues</a></p>
<p> </p>
