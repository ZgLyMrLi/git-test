app.service('seckillOrderService',function ($http) {

    this.findAll = function () {
        return $http.get('../seckillOrder/findAll.do');
    }

})