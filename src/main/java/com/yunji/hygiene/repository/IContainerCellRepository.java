package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ContainerCellPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * IContainerCellRepository：货柜格子表（ContainerCellPO）JPA 数据访问接口
 *
 * 主要用途：
 * - 继承 JpaRepository，获得基础 CRUD 能力
 * - 提供按柜子ID查询格子列表、查询指定格子（默认 ordinal=1）的方法
 *
 * 约定：
 * - delFlag=0 表示未删除（逻辑删除过滤）
 */
public interface IContainerCellRepository extends JpaRepository<ContainerCellPO, Long> {

    /**
     * 查询某个柜子下的全部格子（过滤逻辑删除 delFlag=0）
     *
     * @param containerId 货柜ID
     * @return 格子列表
     */
    @Transactional
    @Query("select c from ContainerCellPO c where c.containerId=:containerId and c.delFlag=0")
    List<ContainerCellPO> getCellList(@Param("containerId") Long containerId);

    /**
     * 查询某个柜子下的第1格（ordinal=1，过滤逻辑删除 delFlag=0）
     * - 常用于单格柜/默认主格子场景
     *
     * @param containerId 货柜ID
     * @return 第1格记录（不存在则返回 null）
     */
    @Transactional
    @Query("select c from ContainerCellPO c where c.containerId=:containerId and c.ordinal=1 and c.delFlag=0")
    ContainerCellPO getCell(@Param("containerId") Long containerId);
}
