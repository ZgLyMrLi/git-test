app.controller('cartController',function ($scope ,cartService) {

    //数量的加减
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if(response.success){
                    $scope.findCartList();
                }else {
                alert(response.message);
                }
            }
        )
    }

    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                add();
            }
        )
    }
    
    add = function () {
        $scope.sum = cartService.sum($scope.cartList);
    }

    $scope.findAddressByName = function () {
        cartService.findAddressByName().success(
            function (response) {
                $scope.addressList = response;
                addAddress();
            }
        )
    }

    addAddress = function () {
        for (var i = 0; i < $scope.addressList.length; i++) {
            if($scope.addressList[i].isDefault==1) {
                $scope.optionAddress = JSON.parse(JSON.stringify($scope.addressList[i]));
                break;
            }
        }
    }

    $scope.changeAddress = function (address) {
        $scope.optionAddress = address;
    }

    $scope.order = {paymentType:'1'}

    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type
    }


    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.optionAddress.address;
        $scope.order.receiverMobile = $scope.optionAddress.mobile;
        $scope.order.receiver = $scope.optionAddress.contact;
        cartService.submitOrder($scope.order).success(
            function (response) {
                if(response.success){
                    //页面跳转到支付
                    if($scope.order.paymentType=='1'){//微信支付
                        location.href = "pay.html";
                    }else {//如果是货到付款
                        location.href = "paysuccess.html";
                    }
                }else {
                    alert(response.message)
                }
            }
        )
    }

})