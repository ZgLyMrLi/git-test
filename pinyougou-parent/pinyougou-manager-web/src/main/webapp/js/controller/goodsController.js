 //控制层 
app.controller('goodsController' ,function($scope,$location,$controller,typeTemplateService ,itemCatService,goodsService){
	
	$controller('baseController',{$scope:$scope});//继承

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




	//批量删除
	$scope.dele=function(){
		//获取选中的复选框
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}
			}
		);
	}

	//$scope.searchEntity={};//定义搜索对象

	//搜索
	$scope.search=function(page,rows){
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
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

    //更新状态
    $scope.updateStatus = function (status) {
		goodsService.updateStatus($scope.selectIds,status).success(
			function (response) {
				if(response.success){
					$scope.reloadList();//刷新页面
					$scope.selectIds = [];
				}else {
					alert(response.message);
				}
            }
		)
    }
});	
