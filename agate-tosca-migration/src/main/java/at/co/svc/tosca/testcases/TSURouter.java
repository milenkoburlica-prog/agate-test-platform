package at.co.svc.tosca.testcases;

import java.util.List;

import at.co.svc.aga.transformator.utils.MigrationLog;
import at.co.svc.tosca.tsu.dto.ToscaNode;

public class TSURouter {

    private final TSUClassifier classifier;

    public TSURouter(TSUClassifier classifier) {
        this.classifier = classifier;
    }

    public void route(List<ToscaNode> nodes) {

        for (ToscaNode node : nodes) {

            String classification =
                    classifier.classify(node);

            MigrationLog.debug(
                    classification
                            + ": "
                            + node.getName()
            );
        }
    }
}
