app.service('seckillService',function ($http) {

    this.findAll = function () {
        return $http.get('../managerSeckill/findAll.do')
    }

    this.findOne = function (id) {
        return $http.get('../managerSeckill/findOne.do?id='+id)
    }

    this.dele = function (ids) {
        return $http.get('../managerSeckill/delete.do?ids='+ids)
    }

    this.add = function (entity) {
        return $http.post('../managerSeckill/add.do',entity)
    }

    this.update = function (entity) {
        return $http.post('../managerSeckill/update.do',entity)
    }

    this.findCheckedSeckillGoods = function () {
        return $http.post('../managerSeckill/findCheckedSeckillGoods.do')
    }

    this.alterStatus = function (id,status) {
        return $http.post('../managerSeckill/alterStatus.do?id='+id+'&status='+status)
    }

})