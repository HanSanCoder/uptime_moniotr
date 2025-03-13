package io.hansan.monitor.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.mapper.TagMapper;
import io.hansan.monitor.model.Tag;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/12 17:47
 * @Description：TODO
 */
@Component
public class TagService extends ServiceImpl<TagMapper, Tag>{

    @Inject
    private TagMapper tagMapper;

    public List<String> findAllTags() {
        return tagMapper.findAllTags();
    }

    public List<Tag> listAll() {
        return this.list();
    }
}