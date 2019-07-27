app.controller('searchController', function ($scope,$location, searchService) {

    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 20,
        'sort': '',
        'sortField': ''
    }

    $scope.resultMap = {};
    //搜索
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                buildPageLabel();
            }
        )
    }

    $scope.priceStatus = 0;//默认升序
    //价格排序
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    $scope.alterPageNo = function (page) {
        if (page < 1 || page > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = page;
        $scope.search();
    }

    buildPageLabel = function () {
        $scope.pageLabel = [];
        var firstPage = 1;
        var lastPage = $scope.resultMap.totalPages;
        $scope.firstDot = true;//开始有点
        $scope.lastDot = true;//最后有点

        if ($scope.resultMap.totalPages > 5) {
            if ($scope.searchMap.pageNo <= 3) {
                $scope.firstDot = false;
                lastPage = 5;
            }
            if ($scope.searchMap.pageNo > 3) {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
            if ($scope.searchMap.pageNo > $scope.resultMap.totalPages - 2) {
                $scope.lastDot = false;
                firstPage = $scope.resultMap.totalPages - 4;
                lastPage = $scope.resultMap.totalPages;
            }
        }

        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    $scope.showTitleName = function () {
        $scope.searchTit = $scope.searchMap.keywords;
    }

    //添加搜索项，改变searchMap的值。
    $scope.addSearchItem = function (key, value) {
        if (key == 'brand' || key == 'category' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }

    //撤销搜索项
    $scope.removeSearchItem = function (key) {
        if (key == 'brand' || key == 'category' || key == 'price') {
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }

    //判断当前页是否为第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }

    //判断当前页是否为最后一页
    $scope.isLastPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    //判断关键字是否是品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    }

    $scope.loadKeyWords = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }
})