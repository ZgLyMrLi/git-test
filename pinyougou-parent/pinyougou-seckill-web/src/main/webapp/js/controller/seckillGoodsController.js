//控制层
app.controller('seckillGoodsController', function ($scope, $location, $interval, seckillGoodsService) {

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        seckillGoodsService.findAll().success(
            function (response) {
                alert(response.length)
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        seckillGoodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        seckillGoodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = seckillGoodsService.update($scope.entity); //修改
        } else {
            serviceObject = seckillGoodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        seckillGoodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    /*$scope.search=function(page,rows){
        seckillGoodsService.search(page,rows,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }*/

    $scope.percent = [];

    $scope.findList = function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list = response;
                for (var i = 0; i < response.length; i++) {
                    var count = (response[i].num - response[i].stockCount) / response[i].num;
                    $scope.percent[response[i].id] = count.toFixed(2) * 100;
                }
            }
        )
    }

    $scope.jumpHtml = function (id) {
        location.href = 'http://localhost:9109/seckill-item.html#?id=' + id;
    }

    $scope.detailPage = function () {
        seckillGoodsService.detailPage($location.search()['id']).success(
            function (response) {
                $scope.oneGood = response;
                second = Math.floor((new Date($scope.oneGood.endTime).getTime() - new Date().getTime()) / 1000);
                time = $interval(function () {
                    second -= 1;
                    $scope.timeStr = convertTimeString(second);
                    if (second <= 0) {
                        $interval.cancel(time)
                    }
                }, 1000)
            }
        )
    }

    convertTimeString = function (second) {
        var days = Math.floor(second / (60 * 60 * 24));
        var hours = Math.floor((second-days*60*60*24)/(60*60));
        var minutes = Math.floor((second-days*60*60*24-hours*60*60)/60);
        var seconds = Math.floor(second-days*60*60*24-hours*60*60-minutes*60);
        var str = "";
        if (days >= 1) {
            str += days+"天";
        }
        return str+hours+":"+minutes+":"+seconds;
    }

    $scope.submitOrder=function(){
        seckillGoodsService.submitOrder($scope.oneGood.id).success(
            function(response){
                if(response.success){
                    alert("下单成功，请在1分钟内完成支付");
                    location.href="pay.html";
                }else{
                    alert(response.message);
                }
            }
        );
    }

});	
