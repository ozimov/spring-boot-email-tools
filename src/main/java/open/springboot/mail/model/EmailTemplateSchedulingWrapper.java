/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package open.springboot.mail.model;

import java.util.Date;
import java.util.Map;

public class EmailTemplateSchedulingWrapper extends EmailSchedulingWrapper {

    String template;
    Map<String, Object> modelObject;
    InlinePicture[] inlinePictures;

    public EmailTemplateSchedulingWrapper(Email email, Date scheduledDate, int priority, String template, Map<String, Object> modelObject, InlinePicture[] inlinePictures) {
        super(email, scheduledDate, priority);
        this.template = template;
        this.modelObject = modelObject;
        this.inlinePictures = inlinePictures;
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, Object> getModelObject() {
        return modelObject;
    }

    public InlinePicture[] getInlinePictures() {
        return inlinePictures;
    }
}
