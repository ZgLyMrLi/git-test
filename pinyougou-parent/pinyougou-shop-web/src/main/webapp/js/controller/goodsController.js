//控制层
app.controller('goodsController', function ($scope,$location, $controller, typeTemplateService, goodsService, uploadService, itemCatService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        var id = $location.search()['id'];
        if(id==null){
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //富文本编辑器
                editor.html($scope.entity.goodsDesc.introduction);
                //显示图片
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格选择
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                //转换SKU列表中的规格对象
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }
            }
        );
    }

    //增加商品
    $scope.add = function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    alert("新增成功");
                    $scope.entity = {};
                    editor.html("");//清空富文本编辑器
                    location.reload();
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //保存
    $scope.save = function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if($scope.entity.goods.id!=null){
            serviceObject = goodsService.update($scope.entity);
        }else {
            serviceObject = goodsService.add($scope.entity);
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert("保存成功");
                    location.href = "goods.html";
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //上传图片
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {//如果上传成功，取出url
                $scope.image_entity.url = response.message;//设置文件地址
            } else {
                alert(response.message);
            }
        }).error(function () {
            alert("上传发生错误");
        });
    };

    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}}

    //将当前上传的图片实体存入图片列表
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.ItemCat1List = response;
            }
        )
    }

    //查询二级商品分类
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.ItemCat2List = response;
            }
        )
    })

    //查询三级商品分类
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.ItemCat3List = response;
            }
        )
    })

    //读取模板Id
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        )
    })

    //读取模板Id后，读取品牌列表
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;//模板对象
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表类型转换
                //扩展属性
                if($location.search()['id']==null){//如果ID等于空，就是增加商品，否则修改的时候会覆盖扩展规格
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                }
            }
        )
    })

    //读取模板Id后，读取扩展属性
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.SpecList = response;
            }
        )
    })

    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        if (object != null) {
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {
                //取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);//移除选项
                //如果选项都取消了，将此条记录移除
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object, 1));
                }
            }
        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]})
        }
    }

    //创建SKU列表
    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];//列表初始化

        var items = $scope.entity.goodsDesc.specificationItems;

        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue)
        }
    }

    addColumn = function (list, columnName, columnValues) {
        var newList = [];
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    $scope.status = ['未审核','已审核','审核未通过','已关闭'];

    $scope.ItemCatList =[];//商品分类列表
    //查询商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.ItemCatList[response[i].id] = response[i].name;
                }
            }
        );
    }

    //判断规格与规格选项是否应该被勾选
    $scope.checkAttributeValue = function (specName,optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items,'attributeName',specName);
        if(object!=null){
            if(object.attributeValue.indexOf(optionName)>=0){//如果能查到则索引就会大于等于0
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    $scope.updateIsMarketable = function (id,isMarketable) {
        $scope.selectIds.push(id);
        goodsService.updateIsMarketable($scope.selectIds,isMarketable).success(
            function (response) {
                alert(response.message);
                $scope.reloadList();
                $scope.selectIds = [];
            }
        )
    }

    $scope.updateIsMarketableAll = function (isMarketable) {
        goodsService.updateIsMarketable($scope.selectIds,isMarketable).success(
            function (response) {
                alert(response.message);
                $scope.reloadList();
                $scope.selectIds = [];
            }
        )
    }

});	
