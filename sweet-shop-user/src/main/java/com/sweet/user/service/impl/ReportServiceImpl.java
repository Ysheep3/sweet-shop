package com.sweet.user.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import com.alipay.api.domain.OrderDetail;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.sweet.api.client.OrderClient;
import com.sweet.api.dto.*;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.exception.OrderBusinessException;
import com.sweet.common.result.Result;
import com.sweet.user.entity.pojo.User;
import com.sweet.user.entity.vo.UserReportVO;
import com.sweet.user.mapper.UserMapper;
import com.sweet.user.service.ReportService;
import com.sweet.user.service.WorkSpaceService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final WorkSpaceService workSpaceService;
    private final UserMapper userMapper;
    private final OrderClient orderClient;

    /**
     * 订单统计
     *
     * @param orderReportDTO
     * @return
     */
    public OrderReportVO getOrdersStatistics(OrderReportDTO orderReportDTO) {
        if (orderReportDTO == null) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }

        Result<OrderReportVO> result = orderClient.ordersStatistics(orderReportDTO);

        if (result == null) {
            throw new OrderBusinessException(MessageConstant.GET_ERROR);
        }

        return result.getData();
    }

    /**
     * 营业额统计
     *
     * @param orderReportDTO
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(OrderReportDTO orderReportDTO) {
        if (orderReportDTO == null) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }

        Result<TurnoverReportVO> result = orderClient.turnoverStatistics(orderReportDTO);

        if (result == null) {
            throw new OrderBusinessException(MessageConstant.GET_ERROR);
        }

        return result.getData();
    }

    /**
     * 用户量统计
     *
     * @param orderReportDTO
     * @return
     */
    public UserReportVO getUserStatistics(OrderReportDTO orderReportDTO) {
        LocalDate begin = orderReportDTO.getBegin();
        LocalDate end = orderReportDTO.getEnd();

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Long newUserCount = userMapper.selectCount(
                    Wrappers.lambdaQuery(User.class)
                            .gt(User::getCreateTime, beginTime));

            Long totalUserCount = userMapper.selectCount(
                    Wrappers.lambdaQuery(User.class)
                            .lt(User::getCreateTime, endTime));

            newUserList.add(newUserCount.intValue());
            totalUserList.add(totalUserCount.intValue());
        }

        return UserReportVO.builder()
                .totalUserList(StringUtils.join(totalUserList, ","))
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 查询销量前十
     *
     * @param orderReportDTO
     * @return
     */
    public SalesTop10ReportVO getTop10(OrderReportDTO orderReportDTO) {
        if (orderReportDTO == null) {
            throw new OrderBusinessException(MessageConstant.DO_ERROR);
        }

        Result<SalesTop10ReportVO> result = orderClient.top10(orderReportDTO);

        if (result == null) {
            throw new OrderBusinessException(MessageConstant.GET_ERROR);
        }

        return result.getData();
    }

    /**
     * 导出Excel表 (近 30天 的数据)
     *
     * @param response
     */
    public void getExcel(HttpServletResponse response) {
        try {
            InputStream ins = this.getClass().getClassLoader().getResourceAsStream("template/report.xlsx");

            XSSFWorkbook excel = new XSSFWorkbook(ins);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            LocalDate today = LocalDate.now();
            LocalDate begin = today.minusDays(30);  // 今天开始算 倒退30
            LocalDate end = today.minusDays(1);   // 直到 昨天


            XSSFRow row = sheet.getRow(1);  // 行 从0开始算
            XSSFCell cell = row.getCell(1); // 列 单元格 从0开始算
            cell.setCellValue(begin + "——" + end);


            BusinessDataVO businessData = workSpaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

            row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(begin.plusDays(i).toString());
                LocalDate date = begin.plusDays(i);
                businessData = workSpaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            ServletOutputStream out = response.getOutputStream();
            excel.write(out);


            ins.close();
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
