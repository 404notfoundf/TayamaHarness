package com.huazai.prd.ingestion.repository;

import com.huazai.prd.ingestion.model.project.Language;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 语言字典数据访问层。
 */
@Repository
public class LanguageRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Language> mapper = (rs, row) -> {
        Language l = new Language();
        l.setId(rs.getLong("id"));
        l.setName(rs.getString("name"));
        l.setSlug(rs.getString("slug"));
        l.setSortOrder(rs.getInt("sort_order"));
        return l;
    };

    public LanguageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Language> findAll() {
        return jdbc.query("SELECT * FROM prd_language ORDER BY sort_order", mapper);
    }
}