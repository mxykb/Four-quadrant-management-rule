package com.example.fourquadrant.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fourquadrant.database.entity.TimerStateEntity;

/**
 * 计时器状态数据访问对象
 */
@Dao
public interface TimerStateDao {
    
    // 插入或更新计时器状态
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateTimerState(TimerStateEntity timerState);
    
    // 更新计时器状态
    @Update
    void updateTimerState(TimerStateEntity timerState);
    
    // 获取计时器状态
    @Query("SELECT * FROM timer_state WHERE id = 'timer_state_singleton' LIMIT 1")
    LiveData<TimerStateEntity> getTimerState();
    
    // 同步获取计时器状态
    @Query("SELECT * FROM timer_state WHERE id = 'timer_state_singleton' LIMIT 1")
    TimerStateEntity getTimerStateSync();
    
    // 清除计时器状态
    @Query("DELETE FROM timer_state")
    void clearTimerState();
    
    // 检查是否存在计时器状态
    @Query("SELECT COUNT(*) FROM timer_state WHERE id = 'timer_state_singleton'")
    int getTimerStateCount();
}