package com.fossil.vn.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.fossil.vn.common.Converter;
import com.fossil.vn.common.Node;

import java.util.Date;
import java.util.List;

@Entity(tableName = "record_session")
public class RecordSession {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "_start_time")
    private long startTime;

    @ColumnInfo(name = "_nodes")
    @TypeConverters({Converter.class})
    private List<Node> nodes;

    @ColumnInfo(name = "_is_finished")
    private boolean isFinished;

    @ColumnInfo(name = "_end_time")
    @TypeConverters({Converter.class})
    private Date endTime;

    public RecordSession(long startTime, List<Node> nodes, boolean isFinished, Date endTime) {
        this.startTime = startTime;
        this.nodes = nodes;
        this.isFinished = isFinished;
        this.endTime = endTime;
    }

    public RecordSession(Date startTime, List<Node> nodes, boolean isFinished) {
        this.startTime = startTime.getTime();
        this.nodes = nodes;
        this.isFinished = isFinished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStartTimeDate() {
        return new Date(startTime);
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }
}
