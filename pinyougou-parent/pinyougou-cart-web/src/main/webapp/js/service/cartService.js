app.service('cartService',function ($http) {
    
    this.addGoodsToCartList = function (itemId, num) {
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num)
    }

    this.findCartList = function () {
        return $http.get('cart/findCartList.do')
    }

    this.findAddressByName = function () {
        return $http.get('address/findAddressByName.do');
    }

    this.sum = function (cartList) {
        var totalValue = {totalNum:0,totalMoney:0}

        for (var i = 0; i < cartList.length; i++) {
            var orderItemList = cartList[i].orderItemList;
            for (var j = 0; j < orderItemList.length; j++) {
                totalValue.totalNum += orderItemList[j].num;
                totalValue.totalMoney += orderItemList[j].totalFee;
            }
        }
        return totalValue;
    }

    this.submitOrder = function (order) {
        return $http.post('order/add.do',order);
    }
    
})