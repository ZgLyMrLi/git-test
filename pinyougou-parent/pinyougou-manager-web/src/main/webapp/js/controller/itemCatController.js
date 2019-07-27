//控制层
app.controller('itemCatController', function ($scope, $controller, itemCatService) {

    $controller('baseController', {$scope: $scope});//继承

    //上级Id
    $scope.parentId = 0;

    $scope.grade = 1;

    $scope.setGrade = function (value) {
        $scope.grade = value;
    }

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        itemCatService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        itemCatService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //保存
    $scope.save = function () {
        $scope.entity.typeId = $scope.typeEntity.typeId.text;
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            alert("修改");
            serviceObject = itemCatService.update($scope.entity); //修改
        } else {
            alert("添加");
            $scope.entity.parentId = $scope.parentId;
            serviceObject = itemCatService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert("修改或添加成功");
                    //重新查询
                    $scope.findByParentId($scope.parentId);//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }
    $scope.sonlist = null;
    //批量删除
    $scope.dele = function () {
        var l = 0;
        for (var i = 0; i < $scope.selectIds.length; i++) {
            itemCatService.findByParentId($scope.selectIds[i]).success(
                function (response) {
                    $scope.sonlist = response;
                    if ($scope.sonlist.length == 0) {
                        //获取选中的复选框
                        itemCatService.dele($scope.selectIds).success(
                            function (response) {
                                if (response.success) {
                                    $scope.setGrade(1);
                                    $scope.selectList({id: 0});
                                    $scope.selectIds = [];
                                }
                            }
                        );
                    }
                }
            );

        }
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        itemCatService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //根据父Id查询商品分类
    $scope.findByParentId = function (parentId) {
        $scope.parentId = parentId;
        itemCatService.findByParentId(parentId).success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    $scope.selectList = function (p_entity) {

        if ($scope.grade == 1) {
            $scope.entity_1 = null;
            $scope.entity_2 = null;
        }
        if ($scope.grade == 2) {
            $scope.entity_1 = p_entity;
            $scope.entity_2 = null;
        }
        if ($scope.grade == 3) {
            $scope.entity_2 = p_entity;
        }
        $scope.findByParentId(p_entity.id);
    }

    //查询实体
    $scope.typeList = {data: []};
    $scope.findOne = function (id) {
        itemCatService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //查询模块
    $scope.findTypeTem = function () {
        $scope.typeList = {data: []};
        itemCatService.findTypeTem().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.typeList.data.push({id: i, text: response[i].text.toString()});
                }
            }
        )
    }


});	
