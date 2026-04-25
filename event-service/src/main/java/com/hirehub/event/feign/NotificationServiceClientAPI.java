package com.hirehub.event.feign;

import com.hirehub.event.dtos.CandidatureChangedDTO;
import com.hirehub.event.dtos.CandidatureDTO;
import com.hirehub.event.dtos.EntretienPlanifiedDTO;
import com.hirehub.event.dtos.HtmlContentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//${notification.service.url}
@FeignClient(name = "notification-service", url = "http://localhost:8080", fallback = NotificationServiceClientFallback.class)
public interface NotificationServiceClientAPI {
    /*@GetMapping
    List<Post> getPosts ();

    @GetMapping ( "/{id}" )
    Post getPostById ( @PathVariable Long id);

    @GetMapping ( "/{id}/comments" )
    List<Comment> getCommentsByPostId ( @PathVariable Long id);
    */

    @PostMapping("/notifications/candidature-confirmation")
    void sendCandidatureConfirmation ( @RequestBody CandidatureDTO candidature);

    @PostMapping("/notifications/statut-changed")
    void sendStatutChangedNotification(@RequestBody CandidatureChangedDTO candidatureChangedDTO);

    @PostMapping("/notifications/entretien-planification")
    void sendEntretienPlanification( @RequestBody EntretienPlanifiedDTO candidatureChangedDTO);

    @PostMapping("/notifications/html-email")
    void sendHtmlEmail(@RequestBody HtmlContentDTO htmlContentDTO);

    /*@PutMapping ( "/{id}" )
    Post updatePost ( @PathVariable Long id, @RequestBody Post post);

    @DeleteMapping ( "/{id}" )
    void deletePost ( @PathVariable Long id);*/
}
