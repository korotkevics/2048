package ch.korotkevics.play2048.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataSettingsRepository extends JpaRepository<SettingsEntity, String> {
}
