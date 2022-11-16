package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequestor_Id(Long requestId, Sort sort);

    @Query("select r from Request r " +
            "where r.requestor.id not in (?1)")
    List<Request> findAllByOtherUsers(Long userId, Pageable pageable);

}
