app.controller('loginController',function ($scope,loginService) {

    $scope.showLoginName = function () {
        loginService.showLoginName().success(
            function (response) {
                $scope.loginName = response.name;
            }
        )
    }

})