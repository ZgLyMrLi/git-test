app.controller('seckillGoodsController',function ($scope,$controller,seckillService) {

    $controller('baseController',{$scope:$scope});

    $scope.search = function () {
        seckillService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        )
    }

    $scope.findOne = function (id) {
        seckillService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        )
    }

    $scope.dele = function () {
        seckillService.dele($scope.selectIds).success(
            function (response) {
                if(response.success){
                    alert(response.message)
                    $scope.search();
                }else {
                    alert(response.message)
                }
            }
        )
    }

    $scope.save = function () {
        var object = null;
        if ($scope.entity.id != null) {
            object = seckillService.update($scope.entity);
        }else {
            object = seckillService.add($scope.entity);
        }
        object.success(
            function (response) {
                if(response.success){
                    alert(response.message)
                    $scope.search();
                }else {
                    alert(response.message)
                }
            }
        )
    }

    $scope.findCheckedSeckillGoods = function () {
        seckillService.findCheckedSeckillGoods().success(
            function (response) {
                $scope.checkedList = response;
            }
        )
    }

    $scope.alterStatus = function () {
        seckillService.alterStatus($scope.selectIds,1).success(
            function (response) {
                if(response.success){
                    alert(response.message)
                    $scope.findCheckedSeckillGoods();
                }else {
                    alert(response.message)
                }
            }
        )
    }

})