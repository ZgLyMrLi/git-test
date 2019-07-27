app.service('loginSevice',function ($http) {

    this.login = function () {
        return $http.get('../login/name.do');
    }

});