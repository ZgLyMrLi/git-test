app.service('contentService',function ($http) {

    //根据广告分类ID查询广告
    this.findByCategortId = function (catagoryId) {
        return $http.get('content/findByCategortId.do?catagoryId='+catagoryId);
    }

})