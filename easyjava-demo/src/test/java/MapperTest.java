import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.kbws.RunApplication;
import xyz.kbws.entity.po.ProductInfo;
import xyz.kbws.entity.query.ProductInfoQuery;
import xyz.kbws.mapper.ProductInfoMapper;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author hsy
 * @date 2023/7/1
 */
@SpringBootTest(classes = RunApplication.class)
@RunWith(SpringRunner.class)
public class MapperTest {

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;

    @Test
    public void selectList(){
        ProductInfoQuery query = new ProductInfoQuery();
        query.setCreateTimeStart("2023-6-5");
        List<ProductInfo> dataList =  productInfoMapper.selectList(query);
        for (ProductInfo productInfo : dataList) {
            System.out.println(productInfo);
        }

        Long count = productInfoMapper.selectCount(query);
        System.out.println(count);
    }

    @Test
    public void insert(){
        ProductInfo productInfo = new ProductInfo();
        productInfo.setCode("10006");
        productInfo.setSkuType(6);
        productInfo.setColorType(0);
        productInfo.setCreateDate(new Date());
        productInfo.setCreateTime(new Date());
        this.productInfoMapper.insert(productInfo);
        System.out.println(productInfo.getId());
    }

    @Test
    public void insertOrUpdate(){
        ProductInfo productInfo = new ProductInfo();
        productInfo.setCode("10006");
        productInfo.setSkuType(6);
        productInfo.setColorType(0);
        productInfo.setCreateDate(new Date());
        productInfo.setCreateTime(new Date());
        productInfo.setProductName("测试");
        this.productInfoMapper.insertOrUpdate(productInfo);
        System.out.println(productInfo.getId());
    }

    @Test
    public void insertBatch(){
        List<ProductInfo> productInfoList = new ArrayList<>();
        ProductInfo productInfo = new ProductInfo();

        productInfo = new ProductInfo();
        productInfo.setCreateDate(new Date());
        productInfo.setCode("0000002");
        productInfoList.add(productInfo);

        productInfo = new ProductInfo();
        productInfo.setCreateDate(new Date());
        productInfo.setCode("0000003");
        productInfoList.add(productInfo);
        productInfoMapper.insertBatch(productInfoList);
    }

    @Test
    public void insertBatchOrUpdate(){
        List<ProductInfo> productInfoList = new ArrayList<>();
        ProductInfo productInfo = new ProductInfo();

        productInfo = new ProductInfo();
        productInfo.setCreateDate(new Date());
        productInfo.setCode("0000002");
        productInfo.setProductName("name002");
        productInfoList.add(productInfo);

        productInfo = new ProductInfo();
        productInfo.setCreateDate(new Date());
        productInfo.setCode("0000003");
        productInfo.setProductName("name003");
        productInfoList.add(productInfo);
        productInfoMapper.insertOrUpdateBatch(productInfoList);
    }

    @Test
    public void selectByKey(){
        ProductInfo productInfo1 = productInfoMapper.selectById(22);
        System.out.println(productInfo1.toString());
        ProductInfo productInfo2 = productInfoMapper.selectByCode("0000002");
        System.out.println(productInfo2.toString());
        ProductInfo productInfo3 = productInfoMapper.selectBySkuTypeAndColorType(5,3);
        System.out.println(productInfo3.toString());
    }

    @Test
    public void updateByKey(){
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductName("update by 1");
        productInfoMapper.updateById(productInfo, 1);
        productInfo = new ProductInfo();
        productInfo.setProductName("update by code");
        Long aLong = productInfoMapper.updateById(productInfo, 2);
        System.out.println(aLong);
    }

    @Test
    public void deleteByKey(){
        productInfoMapper.deleteById(1);
        productInfoMapper.deleteById(2);
    }
}
