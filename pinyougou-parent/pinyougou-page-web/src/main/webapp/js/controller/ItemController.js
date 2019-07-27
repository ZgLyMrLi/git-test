app.controller('itemController',function ($scope,$http) {

	//对数量的加减
	$scope.addNum = function(x){
		$scope.num = parseInt($scope.num);
	
		$scope.num +=x;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	
	$scope.specOptionList = {}//存储用户选择的规格

	//用户选择规格
	$scope.optionSpec = function (attrName,attrValue) {
	    $scope.specOptionList[attrName] = attrValue;
		searchSku();//查询SKU

	}

	$scope.isSelected = function (attrName,attrValue) {
	
	    if($scope.specOptionList[attrName] == attrValue){
		
		    return true;
	    }else {
	
		    return false;
	    }
	}
	
	$scope.sku = {};//存储当前的SKU
	
	//加载默认的SKU
	$scope.loadSku = function(){
		$scope.sku = skuList[0];
		$scope.specOptionList = JSON.parse(JSON.stringify($scope.sku.spec));
	}
	
	//匹配两个对象是否相等
	alterDy = function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		return true;
	}
	
	//根据规格查找SKU
	searchSku = function(){
		for(var i=0;i<skuList.length;i++){
			if(alterDy(skuList[i].spec,$scope.specOptionList)){
				$scope.sku = skuList[i];
				break;
			}else{
				$scope.sku = {id:0,title:'----------',price:0};
			}
		}
	}
	
	//添加商品到购物车
	$scope.addToCart = function(){
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
			+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
			function (response) {
				if(response.success){
					location.href = 'http://localhost:9107/cart.html';
				}else {
					alert(response.message);
				}
            }
		)
	}

})