package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.UpgradeFilePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * IUpgradeFileRepository：升级文件表（UpgradeFilePO）JPA 数据访问接口
 *
 * 主要用途：
 * - 通过升级信息(infoId)反查对应的升级文件(fileId)
 * - 通过 fileId 获取升级文件详情
 * - 按 fileCode 前缀查找“当前版本(current_version=1)”的升级文件（用于按型号/前缀匹配升级包）
 */
public interface IUpgradeFileRepository extends JpaRepository<UpgradeFilePO, Long> {

    /**
     * 通过升级信息ID(infoId)查询对应升级文件ID(fileId)
     * - 先从 tb_upgrade_info 根据 info_id 找到 task_code
     * - 再从 tt_upgrade_task 根据 task_code 找到 file_id
     *
     * @param infoId 升级信息ID
     * @return 升级文件ID（找不到返回 null）
     */
    @Query(value = " select file_id from tt_upgrade_task where task_code=(select task_code from tb_upgrade_info where info_id=:infoId) ",
            nativeQuery = true)
    Long getFileIdByInfoId(@Param("infoId") Long infoId);

    /**
     * 根据文件ID查询升级文件实体（JPQL，面向实体 UpgradeFilePO）
     *
     * @param fileId 升级文件ID
     * @return 升级文件记录；不存在返回 null
     */
    @Transactional
    @Query("SELECT u FROM UpgradeFilePO u WHERE u.id=:fileId")
    UpgradeFilePO getFileById(@Param("fileId") Long fileId);

    /**
     * 按 fileCode 前缀匹配升级文件，并只取“当前版本(current_version=1)”的一条记录
     *
     * @param fileCode 文件编码前缀（例如 厂家-项目-型号-芯片 等拼出来的前缀）
     * @return 匹配到的当前版本升级文件；无则返回 null
     */
    @Transactional
    @Query(value = "SELECT * FROM t_upgrade_file WHERE file_code LIKE CONCAT(:fileCode, '%') and current_version=1 LIMIT 1",
            nativeQuery = true)
    UpgradeFilePO getFileByFileCodeLike(@Param("fileCode") String fileCode);
}
