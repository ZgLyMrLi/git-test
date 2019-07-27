<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>demo1</title>
    <meta charset="UTF-8">
</head>
<body>
    <#include "head.ftl">
    <#--注释不会输出-->
    <!--此注释会输出到源代码中-->
    ${name},你好。${msg}

    <#assign linkman="lyf"><br/>
    ${linkman}<br>

    <#assign linkman={"one":"1","two":"2"}><br/>
    ${linkman.one}<br>
    ${linkman.two}<br>
    集合大小：${linkman?size}<br>

    <#assign text='{"one":"1","two":"2"}'><br/>
    <#assign data=text?eval><br/>
    ${data.one}+${data.two}<br>

    当前日期：${date?date}<br>
    当前时间：${date?time}<br>
    当前日期+时间：${date?datetime}<br>
    日期格式化：${date?string('yyyy年MM月dd日 hh:mm:ss')}<br>

    数字未格式化：${number}
    数字格式化：${number?c}

    <#if success=true>
        您已通过
    <#else>
        您未通过
    </#if>
    <br>
    ----商品列表----<br>
    <#list goodsList as goods>
        ${goods_index}
        商品名称：${goods.name}
        商品价格：${goods.price}<br>
    </#list>
    <br>

    <#if aaa??>
        aaa存在
        <#else>
            aaa不存在
    </#if>
    <br>

    ${bbb!'bbb没有被赋值'}

</body>
</html>