app.controller('payController',function ($scope,payService,$location) {

    $scope.createNative = function () {
        payService.createNative().success(
            function (response) {
                $scope.money = (response.total_fee/100).toFixed(2);
                $scope.out_trade_no = response.out_trade_no;

                var qr = new QRious(
                    {
                        element:document.getElementById('qrious'),
                        size:250,
                        value:response.code_url,
                        level:'H'
                    }
                )

                queryPayStatus();
            }
        )
    }

    queryPayStatus = function () {
        payService.queryPayStatus($scope.out_trade_no).success(
            function (response) {
                if(response.success){
                    location.href = "paysuccess.html#?money="+$scope.money;
                }else {
                    if('二维码超时'==response.message){
                        $scope.createNative();
                    }else {
                        location.href = "payfail.html";
                    }
                }
            }
        )
    }

    $scope.showMoney = function () {
        return $location.search()['money'];
    }

})