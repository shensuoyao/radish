<#assign base=request.contextPath />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>添加Agent组</title>
    <link rel="stylesheet" href="${base}/static/layui/css/layui.css">
    <link rel="stylesheet" href="${base}/static/css/style.css">
    <link rel="icon" href="${base}/static/image/code.png">
</head>
<body class="body">

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 20px;">
    <legend>添加 Agent Group</legend>
</fieldset>

<form class="layui-form" action="${base}/portal/agent-group-save">
    <div class="layui-form-item">
        <label class="layui-form-label">组名</label>
        <div class="layui-input-block">
            <input type="text" name="groupName" lay-verify="title" autocomplete="off" placeholder="Group Name" class="layui-input">
        </div>
    </div>

    <div class="layui-form-item">
        <div class="layui-input-block">
            <button class="layui-btn" lay-submit="" lay-filter="groupSave">立即提交</button>
            <button type="reset" class="layui-btn layui-btn-primary">重置</button>
            <a class="layui-btn layui-btn-primary" href="${base}/portal/agent-group">返回</a>
        </div>
    </div>
</form>
<!-- 通用-970*90 -->

<script src="${base}/static/layui/layui.js" charset="utf-8"></script>
<script>
    layui.use(['form', 'layedit', 'laydate'], function(){
        var form = layui.form
                ,layer = layui.layer
                ,layedit = layui.layedit

        //监听提交
    form.on('submit(groupSave)', function(data){
        layer.alert(JSON.stringify(data.field), {
            title: '最终的提交信息'
                  });
        return false;
          });

    });
</script>
</body>
</html>