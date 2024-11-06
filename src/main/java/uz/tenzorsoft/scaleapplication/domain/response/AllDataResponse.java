package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AllDataResponse {
    private List<TableViewData> actionDto;
    private List<UserResponse> userDto;
    private List<CargoResponse> weighingDto;
    private List<AttachResponse> attachmentDto;
}
