package com.huazai.prd.ingestion.repository;

import com.huazai.prd.ingestion.model.project.Framework;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 框架字典数据访问层。
 */
@Repository
public class FrameworkRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Framework> mapper = (rs, row) -> {
        Framework f = new Framework();
        f.setId(rs.getLong("id"));
        f.setLanguageId(rs.getLong("language_id"));
        f.setName(rs.getString("name"));
        f.setSlug(rs.getString("slug"));
        f.setSortOrder(rs.getInt("sort_order"));
        return f;
    };

    public FrameworkRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Framework> findAll() {
        return jdbc.query("SELECT * FROM prd_framework ORDER BY sort_order", mapper);
    }

    public List<Framework> findByLanguageId(Long languageId) {
        return jdbc.query("SELECT * FROM prd_framework WHERE language_id=? ORDER BY sort_order", mapper, languageId);
    }
}