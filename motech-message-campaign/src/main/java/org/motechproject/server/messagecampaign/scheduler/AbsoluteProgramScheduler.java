package org.motechproject.server.messagecampaign.scheduler;

import org.motechproject.scheduler.MotechSchedulerService;
import org.motechproject.server.messagecampaign.EventKeys;
import org.motechproject.server.messagecampaign.builder.SchedulerPayloadBuilder;
import org.motechproject.server.messagecampaign.contract.EnrollRequest;
import org.motechproject.server.messagecampaign.domain.campaign.AbsoluteCampaign;
import org.motechproject.server.messagecampaign.domain.message.AbsoluteCampaignMessage;

import java.util.HashMap;

public class AbsoluteProgramScheduler extends MessageCampaignScheduler {

    private EnrollRequest enrollRequest;
    private AbsoluteCampaign campaign;

    public AbsoluteProgramScheduler(MotechSchedulerService schedulerService,
                                    EnrollRequest request, AbsoluteCampaign campaign) {
        this.campaign = campaign;
        this.schedulerService = schedulerService;
        this.enrollRequest = request;
    }

    private void scheduleJob(AbsoluteCampaignMessage message) {
        String jobId = EventKeys.BASE_SUBJECT + campaign.name() + "." + message.name() + "." + enrollRequest.externalId();

        HashMap params = new SchedulerPayloadBuilder()
                .withJobId(jobId)
                .withCampaignName(campaign.name())
                .withMessageKey(message.messageKey())
                .withExternalId(enrollRequest.externalId())
                .payload();

        scheduleJobOn(enrollRequest.reminderTime(), message.date(), params);
    }

    @Override
    public void scheduleJobs() {
        for (AbsoluteCampaignMessage message : campaign.messages()) {
            scheduleJob(message);
        }
    }
}
