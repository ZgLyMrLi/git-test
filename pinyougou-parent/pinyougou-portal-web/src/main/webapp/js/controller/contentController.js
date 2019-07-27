app.controller('contentController',function ($scope,contentService) {

    $scope.contentList=[];//广告列表

    $scope.findByCategortId = function (categortId) {
        contentService.findByCategortId(categortId).success(
            function (response) {
                $scope.contentList[categortId] = response;
            }
        )
    }

    $scope.search = function () {
        location.href = "http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }

})