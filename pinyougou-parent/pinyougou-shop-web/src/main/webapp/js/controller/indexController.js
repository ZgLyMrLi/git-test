app.controller('indexController',function ($scope, loginSevice) {

    //显示当前用户名
    $scope.showLoginName = function () {
        loginSevice.login().success(
            function (response) {
                $scope.loginName = response.loginName;
            }
        )
    }

})