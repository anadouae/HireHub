package com.hirehub.frontend.candidature;

import com.hirehub.frontend.auth.FrontendUserAccount;
import com.hirehub.frontend.auth.FrontendUserRepository;
import com.hirehub.frontend.auth.HirehubUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class CandidateProfileService {

    private final FrontendUserRepository userRepository;
    private final ApplicationUploadService applicationUploadService;

    public CandidateProfileService(
            FrontendUserRepository userRepository,
            ApplicationUploadService applicationUploadService
    ) {
        this.userRepository = userRepository;
        this.applicationUploadService = applicationUploadService;
    }

    @Transactional(readOnly = true)
    public CandidateProfileView load(HirehubUserDetails user) {
        FrontendUserAccount account = requireAccount(user);
        UUID candidatId = account.getId();
        return new CandidateProfileView(
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getDefaultCvPath(),
                applicationUploadService.hasDefaultCv(candidatId)
        );
    }

    @Transactional
    public void save(HirehubUserDetails user, String phone, MultipartFile defaultCv) throws IOException {
        FrontendUserAccount account = requireAccount(user);
        UUID candidatId = account.getId();
        if (phone != null) {
            account.setPhone(phone.trim());
        }
        if (defaultCv != null && !defaultCv.isEmpty()) {
            String path = applicationUploadService.storeDefaultCv(candidatId, defaultCv);
            account.setDefaultCvPath(path);
        }
        userRepository.saveAndFlush(account);
    }

    @Transactional(readOnly = true)
    public String defaultCvPathForApply(UUID candidatId) {
        return userRepository.findById(candidatId)
                .map(FrontendUserAccount::getDefaultCvPath)
                .filter(path -> path != null && !path.isBlank())
                .filter(path -> applicationUploadService.resolveStoredFile(path).isPresent())
                .orElse(null);
    }

    private FrontendUserAccount requireAccount(HirehubUserDetails user) {
        return userRepository.findById(user.getId())
                .or(() -> userRepository.findByEmailIgnoreCase(user.getUsername()))
                .orElseThrow(() -> new IllegalStateException(
                        "Compte introuvable. Déconnectez-vous puis reconnectez-vous."));
    }
}
