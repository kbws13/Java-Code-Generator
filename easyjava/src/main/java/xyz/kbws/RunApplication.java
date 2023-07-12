package xyz.kbws;

import xyz.kbws.bean.TableInfo;
import xyz.kbws.builder.*;

import java.util.List;

/**
 * @author hsy
 * @date 2023/6/25
 */
public class RunApplication {
    public static void main(String[] args) {
        List<TableInfo> tableInfoList = BuildTable.getTables();

        BuildBase.execute();

        for (TableInfo tableInfo : tableInfoList) {
            BuildPo.execute(tableInfo);

            BuildQuery.execute(tableInfo);

            BuildMapper.execute(tableInfo);

            BuildMapperXml.execute(tableInfo);

            BuildService.execute(tableInfo);

            BuildServiceImpl.execute(tableInfo);

            BuildController.execute(tableInfo);
        }
    }
}
