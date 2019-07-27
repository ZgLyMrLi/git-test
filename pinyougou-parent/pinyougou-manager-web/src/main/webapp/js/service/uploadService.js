app.service('uploadService', function ($http) {

    //上传文件
    this.uploadFile = function () {
        var formdata = new FormData();
        formdata.append('file', file.files[0]);//file:文件上传框的name  【0】取第一个

        return $http({
            url: '../upload.do',
            method: 'post',
            data:formdata,
            headers:{'Content-Type':undefined},//默认类型为json类型
            transformRequest:angular.identity//对整个表单进行二进制序列化
        });
    }

})