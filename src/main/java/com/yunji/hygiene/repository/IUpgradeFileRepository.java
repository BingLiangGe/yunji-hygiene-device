package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.UpgradeFilePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author : peter-zhu
 * @date : 2025/2/14 09:58
 * @description : TODO
 **/
public interface IUpgradeFileRepository extends JpaRepository<UpgradeFilePO, Long> {


    @Transactional
    @Query("SELECT u FROM UpgradeFilePO u WHERE u.id=:fileId")
    UpgradeFilePO getFileById(@Param("fileId") Long fileId);

    @Transactional
    @Query(value = "SELECT * FROM t_upgrade_file WHERE file_code LIKE CONCAT(:fileCode, '%') and current_version=1 LIMIT 1", nativeQuery = true)
    UpgradeFilePO getFileByFileCodeLike(@Param("fileCode") String fileCode);

}
