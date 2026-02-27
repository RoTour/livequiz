package com.livequiz.backend.application;

import com.livequiz.backend.domain.teacher.TeacherIdentityRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ResolveUserRoleUseCase {

  public static final String INSTRUCTOR_ROLE = "INSTRUCTOR";
  public static final String STUDENT_ROLE = "STUDENT";

  private final TeacherIdentityRepository teacherIdentityRepository;

  public ResolveUserRoleUseCase(TeacherIdentityRepository teacherIdentityRepository) {
    this.teacherIdentityRepository = teacherIdentityRepository;
  }

  public String execute(String principalId) {
    if (principalId == null || principalId.isBlank()) {
      return STUDENT_ROLE;
    }

    return this.teacherIdentityRepository
      .findByPrincipalId(principalId.trim().toLowerCase(Locale.ROOT))
      .filter(teacherIdentity -> teacherIdentity.active())
      .map(teacherIdentity -> INSTRUCTOR_ROLE)
      .orElse(STUDENT_ROLE);
  }
}
