// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.actor;

import java.util.TimerTask;

/**
 * Represents an ongoing timeout
 *
 * @author Vaclav Pech
 */
final class ActorTimerTask extends TimerTask {
    private final AbstractLoopingActor actor;
    private final int id;

    ActorTimerTask(final AbstractLoopingActor actor, final int id) {
        this.actor = actor;
        this.id = id;
    }

    @SuppressWarnings({"CatchGenericClass"})
    @Override
    public void run() {
        try {
            actor.send(Actor.TIMEOUT_MESSAGE);
        } catch (Exception e) {
            actor.handleException(e);
        }
    }

    public int getId() {
        return id;
    }
}
