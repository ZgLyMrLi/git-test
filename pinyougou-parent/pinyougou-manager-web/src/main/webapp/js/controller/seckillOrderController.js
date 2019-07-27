app.controller('seckillOrderController',function ($scope,seckillOrderService) {

    $scope.search = function(){
        seckillOrderService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        )
    }

})