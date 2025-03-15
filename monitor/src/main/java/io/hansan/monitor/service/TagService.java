package io.hansan.monitor.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.dto.Result;
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
    public Result addTag(Tag tag) {
        boolean saved = this.save(tag);
        Result result = new Result();
        if (saved) {
            result.setTagId(tag.getId());
            result.setOk(true);
            result.setTag(tag);
            result.setMsg("添加标签成功");
        } else {
            result.setOk(false);
            result.setMsg("添加标签失败");
        }
        return result;
    }

    public Result addMonitorTag(Integer tagId, Integer monitorId) {
        boolean saved = tagMapper.addMonitorTag(tagId, monitorId);
        Result result = new Result();
        if (saved) {
            result.setOk(true);
            result.setMessage("添加监控项标签成功");
        } else {
            result.setOk(false);
            result.setMessage("添加监控项标签失败");
        }
        return result;
    }

    public Result deleteMonitorTag(Integer tagId, Integer monitorId) {
        boolean deleted = tagMapper.deleteMonitorTag(tagId, monitorId);
        Result result = new Result();
        if (deleted) {
            result.setOk(true);
            result.setMsg("删除监控项标签成功");
        } else {
            result.setOk(false);
            result.setMsg("删除监控项标签失败");
        }
        return result;
    }

    public Result deleteTag(Integer tagId) {
        boolean deleted = this.removeById(tagId);
        Result result = new Result();
        result.setOk(deleted);
        result.setMsg("删除标签成功");
        return result;
    }

    public Result editTag(Tag tag) {
        boolean updated = this.updateById(tag);
        Result result = new Result();
        if (updated) {
            result.setOk(true);
            result.setMsg("修改标签成功");
        } else {
            result.setOk(false);
            result.setMsg("修改标签失败");
        }
        return result;
    }
}