//控制层
app.controller('userController', function ($scope, $controller, userService) {

    $scope.reg = function () {
        if ($scope.entity.password != $scope.password) {
            $scope.password = "";
            $scope.entity.password = "";
            return;
        } else {
            userService.add($scope.entity,$scope.smsCode).success(
                function (response) {
                    $scope.password = "";
                    $scope.entity.password = "";
                    $scope.entity.username = "";
                    $scope.entity.phone = "";
                    $scope.smsCode = "";
                    alert(response.message);
                }
            )
        }
    }

    $scope.yzm = function () {
        if ($scope.entity.phone == null || $scope.entity.phone == "") {
            alert("请填入手机号")
            return;
        }
        userService.yzm($scope.entity.phone).success(
            function (response) {
                $scope.confirm = response.success;
                alert(response.message)
            }
        )
    }

});	
