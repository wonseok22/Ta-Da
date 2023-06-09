package com.tada.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tada.domain.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

	List<User> findAllByRoom_IdOrderByCreateTimeDesc(Long roomId);
	User findByRoomIdAndNick(Long roomId, String nick);

	Long countByRoom_Id(Long roomId);
}
